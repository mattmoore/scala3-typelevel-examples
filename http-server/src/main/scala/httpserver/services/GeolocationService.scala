package httpserver.services

import cats.Monad
import cats.syntax.all.*
import org.typelevel.log4cats.Logger

trait GeolocationService[F[_]] {
  def hello(name: String): F[String]
}

object GeolocationService {
  def apply[F[_]: Monad: Logger](): GeolocationService[F] = new GeolocationService[F] {
    override def hello(name: String): F[String] =
      for {
        _      <- Logger[F].info(s"Invoked hello($name)")
        result <- Monad[F].pure(s"Hello, $name.")
      } yield result
  }
}
