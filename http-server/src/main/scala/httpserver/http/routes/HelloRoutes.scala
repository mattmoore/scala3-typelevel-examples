package httpserver.http.routes

import cats.*
import cats.effect.*
import cats.implicits.*
import httpserver.http.*
import httpserver.services.*
import org.http4s.*
import org.http4s.dsl.*

object HelloRoutes {
  def apply[F[_]: Async]()(using
      dsl: Http4sDsl[F],
      helloService: HelloService[F],
  ): HttpRoutes[F] = {
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "hello" / name =>
        helloService
          .hello(name)
          .flatMap(Ok(_))
          .handleErrorWith(_ => InternalServerError())
      case GET -> Root / "healthcheck" =>
        Ok()
    }
  }
}
