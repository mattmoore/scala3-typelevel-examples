package geolocation.services

import cats.*
import cats.effect.*
import cats.syntax.all.*
import geolocation.domain.*
import geolocation.repositories.*
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait GeolocationService[F[_]] {
  def getCoords(query: AddressQuery): F[Either[String, GpsCoords]]
  def create(address: Address): F[Either[String, Unit]]
}

object GeolocationService {
  def apply[F[_]: Async: SelfAwareStructuredLogger](
      repo: AddressRepository[F],
  ): GeolocationService[F] = new GeolocationService[F] {
    override def getCoords(query: AddressQuery): F[Either[String, GpsCoords]] =
      for {
        _ <- SelfAwareStructuredLogger[F].info(
          Map("function_name" -> "getCoords", "function_args" -> s"$query"),
        )(
          s"Invoked getCoords($query)",
        )
        result <- repo.getByAddress(query).map {
          case Some(address) => Right(address.coords)
          case None          => Left("No address found.")
        }
      } yield result

    override def create(address: Address): F[Either[String, Unit]] =
      for {
        _      <- SelfAwareStructuredLogger[F].info(s"Invoked create($address)")
        result <- repo.insert(address)
      } yield result
  }
}
