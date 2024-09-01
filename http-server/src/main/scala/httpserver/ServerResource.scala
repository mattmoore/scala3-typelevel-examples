package httpserver

import cats.*
import cats.effect.*
import cats.implicits.*
import com.comcast.ip4s.*
import fs2.io.net.Network
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.http4s.ember.server.*
import org.http4s.implicits.*
import org.http4s.server.Server
import org.typelevel.log4cats.Logger

import domain.*
import services.*

object ServerResource {
  def apply[F[_]: Async: Network](using
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

    case class AddressRequest(
        street: String,
        city: String,
        state: String,
    )

    implicit val addressRequestEntityDecoder: EntityDecoder[F, AddressRequest] = jsonOf[F, AddressRequest]

    val geolocationRoutes = HttpRoutes.of[F] {
      case req @ POST -> Root / "coords" =>
        for {
          request <- req.as[AddressRequest]
          addressQuery = Address(
            id = 1,
            street = request.street,
            city = request.city,
            state = request.state,
            coords = GpsCoords(0, 0),
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
      .withPort(port"8080")
      .withHttpApp(allRoutes)
      .withLogger(logger)
      .build
}
