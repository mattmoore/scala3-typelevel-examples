package httpserver

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*

import services.*

final case class Resources[F[_]](
    logger: SelfAwareStructuredLogger[F],
    httpServer: Resource[F, Server]
)

def helloRoutes[F[_]: Sync](using helloService: HelloService[F]): HttpApp[F] = {
  val dsl = Http4sDsl[F]
  import dsl.*
  HttpRoutes
    .of[F] {
      case GET -> Root / "hello" / name =>
        helloService
          .hello(name)
          .flatMap { result =>
            Ok(result)
          }
          .handleErrorWith(_ => InternalServerError())
      case GET -> Root / "healthcheck" =>
        Ok()
    }
    .orNotFound
}

object Resources {
  def make[F[_]: Async: Network]: Resource[F, Resources[F]] = {
    Resource.eval {
      given LoggerFactory[F]                     = Slf4jFactory.create[F]
      given logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
      given HelloService[F]                      = HelloService()
      val httpServer: Resource[F, Server] = EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(helloRoutes)
        .withLogger(logger)
        .build
      Async[F].pure {
        Resources[F](
          logger,
          httpServer
        )
      }
    }
  }
}
