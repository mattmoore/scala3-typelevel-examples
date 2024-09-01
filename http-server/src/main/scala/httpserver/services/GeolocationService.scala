package httpserver.services

import cats.effect.*
import cats.syntax.all.*
import httpserver.domain.*
import httpserver.repositories.*
import org.typelevel.log4cats.Logger

trait GeolocationService[F[_]] {
  def getCoords(address: Address): F[Either[String, GpsCoords]]
}

object GeolocationService {
  def apply[F[_]: Async: Logger]()(using
      repo: AddressRepository[F],
  ): GeolocationService[F] = new GeolocationService[F] {
    override def getCoords(address: Address): F[Either[String, GpsCoords]] =
      for {
        _ <- Logger[F].info(s"Invoked getCoords($address)")
        result <- repo.getByAddress(address).map {
          case Some(address) => Right(address.coords)
          case None          => Left("No address found.")
        }
      } yield result
  }
}
