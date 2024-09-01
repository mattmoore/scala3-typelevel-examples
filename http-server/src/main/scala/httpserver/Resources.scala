package httpserver

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*

import repositories.*
import services.*

final case class Resources[F[_]](
    logger: SelfAwareStructuredLogger[F],
    httpServer: Resource[F, Server],
)

object Resources {
  def make[F[_]: Async: Console: Network]: Resource[F, Resources[F]] =
    Resource.eval {
      given LoggerFactory[F]                     = Slf4jFactory.create[F]
      given logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
      given HelloService[F]                      = HelloService()
      given AddressRepository[F] = AddressRepository(
        host = "localhost",
        port = 5432,
        username = "scala",
        password = "scala",
        database = "geolocation",
      )
      given GeolocationService[F]         = GeolocationService()
      val httpServer: Resource[F, Server] = ServerResource[F]

      Resources[F](
        logger,
        httpServer,
      ).pure
    }
}
