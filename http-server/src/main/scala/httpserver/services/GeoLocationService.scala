package httpserver.services

import cats.Monad
import cats.syntax.all.*
import org.typelevel.log4cats.Logger

trait GeoLocationService[F[_]] {
  def hello(name: String): F[String]
}

object GeoLocationService {
  def apply[F[_]: Monad: Logger](): GeoLocationService[F] = new GeoLocationService[F] {
    override def hello(name: String): F[String] =
      for {
        _      <- Logger[F].info(s"Invoked hello($name)")
        result <- Monad[F].pure(s"Hello, $name.")
      } yield result
  }
}
