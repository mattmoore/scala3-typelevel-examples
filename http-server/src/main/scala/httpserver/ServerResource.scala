package httpserver

import cats.*
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.ember.server.*
import org.http4s.server.Server
import org.typelevel.log4cats.Logger

import services.*

object ServerResource {
  def apply[F[_]: Async: Network](using
      logger: Logger[F],
      helloService: HelloService[F],
      geolocationService: GeolocationService[F]
  ): Resource[F, Server] =
    val dsl = Http4sDsl[F]
    import dsl.*

    val helloRoutes = HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        helloService
          .hello(name)
          .flatMap(Ok(_))
          .handleErrorWith(_ => InternalServerError())
      case GET -> Root / "healthcheck" =>
        Ok()
    }

    val geolocationRoutes = HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        geolocationService
          .hello(name)
          .flatMap(Ok(_))
          .handleErrorWith(_ => InternalServerError())
      case GET -> Root / "healthcheck" =>
        Ok()
    }

    val allRoutes = (helloRoutes <+> geolocationRoutes).orNotFound

    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(allRoutes)
      .withLogger(logger)
      .build
}
