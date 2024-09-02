package httpserver.http

import cats.*
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import httpserver.domain.*
import httpserver.services.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.typelevel.log4cats.Logger

object ServerResource {
  def apply[F[_]: Async: Network](using
      config: Config,
      logger: Logger[F],
      helloService: HelloService[F],
      geolocationService: GeolocationService[F],
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
      case req @ POST -> Root / "coords" =>
        for {
          request <- req.as[requests.CoordsRequest]
          addressQuery = AddressQuery(
            street = request.street,
            city = request.city,
            state = request.state,
          )
          response <- geolocationService
            .getCoords(addressQuery)
            .flatMap {
              case Right(coords) => Ok(coords.asJson)
              case Left(error)   => Ok(error)
            }
            .handleErrorWith(_ => InternalServerError())
        } yield (response)
      case GET -> Root / "healthcheck" =>
        Ok()
    }

    val allRoutes = (helloRoutes <+> geolocationRoutes).orNotFound

    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(config.port).getOrElse(port"8080"))
      .withHttpApp(allRoutes)
      .withLogger(logger)
      .build
}
