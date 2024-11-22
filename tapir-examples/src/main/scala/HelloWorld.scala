import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import sttp.tapir.*
import sttp.tapir.server.http4s.Http4sServerInterpreter

object HelloWorld extends IOApp {
  val helloWorldEndpoint = endpoint.get
    .in("hello" / "world")
    .in(query[String]("name"))
    .out(stringBody)
    .serverLogic[IO](name =>
      IO
        .println(s"Saying hello to: $name")
        .flatMap(_ => IO.pure(Right(s"Hello, $name!"))),
    )

  val helloWorldRoutes: HttpRoutes[IO] = Http4sServerInterpreter[IO]()
    .toRoutes(helloWorldEndpoint)

  override def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(helloWorldRoutes.orNotFound)
      .build
      .useForever
}
