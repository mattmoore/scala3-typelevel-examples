import cats.effect.*
import com.comcast.ip4s.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.prometheus.metrics.core.metrics.Counter
import io.prometheus.metrics.model.registry.PrometheusRegistry
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.jsoniter.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.server.http4s.Http4sServerOptions
import sttp.tapir.server.metrics.EndpointMetric
import sttp.tapir.server.metrics.Metric
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

object JsonBody extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    server.useForever

  def server: Resource[IO, Server] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes.orNotFound)
      .build

  case class Country(name: String)
  case class Author(name: String, country: Country)
  case class Genre(name: String)
  case class Book(title: String, genre: Genre, year: Int, author: Author)

  // val book = Book("The Witcher: The Last Wish", Genre("Fantasy"), 1993, Author("Andrzej Sapkowski", Country("Poland")))

  given JsonValueCodec[Book] = JsonCodecMaker.make

  val customCounterMetric = Counter
    .builder()
    .name("book_custom_metric")
    .help("HTTP responses")
    .labelNames("path", "method", "status", "book_title")
    .register(PrometheusRegistry.defaultRegistry)

  val customMetrics = Metric[IO, Counter](
    customCounterMetric,
    onRequest = { (req, counter, _) =>
      IO(
        EndpointMetric()
          .onResponseBody { (ep, res) =>
            val path       = ep.showPathTemplate()
            val method     = req.method.method
            val status     = res.code.toString()
            val underlying = req.underlying.asInstanceOf[Request[IO]]
            val bodyStringIO = underlying
              .bodyText(implicitly, underlying.charset.getOrElse(Charset.`UTF-8`))
              .compile
              .string
            for {
              bodyString <- bodyStringIO
              parsed = decode[Book](bodyString)
            } yield {
              parsed match {
                case Left(error) => counter.labelValues(path, method, status, "").inc()
                case Right(book) => counter.labelValues(path, method, status, book.title).inc()
              }
            }
          },
      )
    },
  )

  val prometheusMetrics = PrometheusMetrics
    .default[IO]("book")
    .addCustom(customMetrics)

  val serverOptions = Http4sServerOptions
    .customiseInterceptors[IO]
    .metricsInterceptor(prometheusMetrics.metricsInterceptor())
    .options

  val bookEndpointSpec = endpoint.post
    .in("api" / "book")
    .in(jsonBody[Book])
    .out(jsonBody[Book])

  val bookEndpoint = bookEndpointSpec
    .serverLogic[IO](book =>
      IO
        .println(s"Book: ${book}")
        .flatMap(_ => IO.pure(Right(book))),
    )

  val endpoints = List(
    bookEndpoint,
    prometheusMetrics.metricsEndpoint,
  )

  val routes: HttpRoutes[IO] = Http4sServerInterpreter[IO](serverOptions)
    .toRoutes(endpoints)
}
