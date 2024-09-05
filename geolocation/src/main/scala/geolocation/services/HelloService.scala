package geolocation.services

import cats.Monad
import cats.syntax.all.*
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait HelloService[F[_]] {
  def hello(name: String): F[String]
}

object HelloService {
  def apply[F[_]: Monad: SelfAwareStructuredLogger]: HelloService[F] = new HelloService[F] {
    def hello(name: String): F[String] =
      for {
        _      <- SelfAwareStructuredLogger[F].info(s"Invoked hello($name)")
        result <- Monad[F].pure(s"Hello, $name.")
      } yield result
  }
}
