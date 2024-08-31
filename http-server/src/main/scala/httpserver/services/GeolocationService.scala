package httpserver.services

import cats.effect.*
import cats.syntax.all.*
import httpserver.domain.*
import httpserver.repositories.*
import org.typelevel.log4cats.Logger

trait GeolocationService[F[_]] {
  def getCoords(address: Address): F[Option[GpsCoords]]
}

object GeolocationService {
  def apply[F[_]: Async: Logger]()(using
      repo: AddressRepository[F],
  ): GeolocationService[F] = new GeolocationService[F] {
    override def getCoords(address: Address): F[Option[GpsCoords]] =
      for {
        _      <- Logger[F].info(s"Invoked getCoords($address)")
        result <- repo.getByAddress(address).map(_.map(_.coords))
      } yield result
  }
}
