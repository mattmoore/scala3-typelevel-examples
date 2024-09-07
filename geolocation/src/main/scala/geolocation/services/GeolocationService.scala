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
        result <- repo.getByAddress(query).flatMap {
          case Some(address) => address.coords.asRight.pure
          case None =>
            SelfAwareStructuredLogger[F].error(
              Map("function_name" -> "getCoords", "function_args" -> s"$query"),
            )(
              s"Invoked getCoords($query)",
            ) *>
              "No address found.".asLeft.pure
        }
      } yield result

    override def create(address: Address): F[Either[String, Unit]] =
      for {
        _ <- SelfAwareStructuredLogger[F].info(
          Map("function_name" -> "create", "function_args" -> s"$address"),
        )(
          s"Invoked create($address)",
        )
        result <- repo
          .insert(address)
          .flatMap {
            case Right(unit) => ().asRight.pure
            case Left(error) =>
              SelfAwareStructuredLogger[F].error(
                Map("function_name" -> "create", "function_args" -> s"$address"),
              )(
                error,
              ) *>
                error.asLeft.pure
          }
      } yield result
  }
}
