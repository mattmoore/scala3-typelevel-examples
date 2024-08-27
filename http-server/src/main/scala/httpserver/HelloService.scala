package httpserver

import cats.effect.*
import org.typelevel.log4cats.Logger

trait HelloService[F[_]] {
  def hello(name: String): F[String]
}

final class HelloServiceImpl(using logger: Logger[IO]) extends HelloService[IO] {
  def hello(name: String): IO[String] =
    for {
      _      <- logger.info(s"Invoked hello($name)")
      result <- IO.pure(s"Hello, $name.")
    } yield result
}

object HelloService {
  def make(using logger: Logger[IO]): Resource[IO, HelloService[IO]] =
    Resource.eval(
      IO.pure(
        HelloServiceImpl()
      )
    )
}
