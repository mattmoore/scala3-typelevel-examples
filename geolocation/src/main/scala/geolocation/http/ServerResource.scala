package geolocation.http

import cats.*
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import geolocation.domain.*
import geolocation.http.routes.*
import geolocation.services.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.typelevel.log4cats.Logger

object ServerResource {
  def make[F[_]: Async: Network](
      config: Config,
      logger: Logger[F],
      helloService: HelloService[F],
      geolocationService: GeolocationService[F],
  ): Resource[F, Server] = {
    val dsl = Http4sDsl[F]

    val routes: HttpRoutes[F] = List(
      HelloRoutes(dsl, helloService),
      GeolocationRoutes(dsl, geolocationService),
    ).foldK

    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(
        Port
          .fromInt(config.port)
          .getOrElse(port"8080"),
      )
      .withHttpApp(routes.orNotFound)
      .withLogger(logger)
      .build
  }
}
