package geolocation.services

import cats.Monad
import cats.syntax.all.*
import org.typelevel.log4cats.Logger

trait HelloService[F[_]] {
  def hello(name: String): F[String]
}

object HelloService {
  def apply[F[_]: Monad: Logger](): HelloService[F] = new HelloService[F] {
    def hello(name: String): F[String] =
      for {
        _      <- Logger[F].info(s"Invoked hello($name)")
        result <- Monad[F].pure(s"Hello, $name.")
      } yield result
  }
}
