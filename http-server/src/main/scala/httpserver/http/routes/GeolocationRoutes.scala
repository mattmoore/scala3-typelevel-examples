package httpserver.http.routes

import cats.*
import cats.effect.*
import cats.implicits.*
import httpserver.domain.*
import httpserver.http.*
import httpserver.services.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.*
import org.http4s.dsl.*

object GeolocationRoutes {
  def apply[F[_]: Async]()(using
      dsl: Http4sDsl[F],
      geolocationService: GeolocationService[F],
  ): HttpRoutes[F] = {
    import dsl.*

    HttpRoutes.of[F] {
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
  }
}
