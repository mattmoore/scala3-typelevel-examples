package httpserver

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*

final case class Resources[F[_]: Async](
    loggerFactory: LoggerFactory[F],
    httpServer: Resource[F, Server],
    helloService: HelloService[F]
)

def routes(helloService: HelloService[IO]) = HttpRoutes
  .of[IO] { case GET -> Root / "hello" / name =>
    Ok(helloService.hello(name))
  }
  .orNotFound

object Resources {
  def make: Resource[IO, Resources[IO]] = {
    given loggerFactory: LoggerFactory[IO]    = Slf4jFactory.create[IO]
    val logger: SelfAwareStructuredLogger[IO] = LoggerFactory[IO].getLogger
    val helloService: HelloService[IO]        = HelloService.make
    val httpServer: Resource[IO, Server] = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes(helloService))
      .withLogger(logger)
      .build

    Resource.eval(
      IO.pure(
        Resources(
          loggerFactory,
          httpServer,
          helloService
        )
      )
    )
  }
}
