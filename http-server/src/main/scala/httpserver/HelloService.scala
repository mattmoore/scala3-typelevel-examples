package httpserver

import cats.effect.*

trait HelloService[F[_]] {
  def hello(name: String): String
}

final class HelloServiceImpl[F[_]: Async] extends HelloService[F] {
  def hello(name: String): String =
    s"Hello, $name."
}

object HelloService {
  def make[F[_]: Async]: HelloService[F] =
    new HelloServiceImpl()
}
