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

import domain.*
import repositories.*
import services.*
import http.*

final case class Resources[F[_]](
    config: Config,
    logger: SelfAwareStructuredLogger[F],
    httpServer: Server,
)

object Resources {
  def make[F[_]: Async: Console: Network]: Resource[F, Resources[F]] =
    for {
      config <- Resource.eval(Config.load[F])
      given Config                         = config
      given LoggerFactory[F]               = Slf4jFactory.create[F]
      logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
      given SelfAwareStructuredLogger[F]   = logger
      given HelloService[F]                = HelloService()
      given AddressRepository[F] = AddressRepository(
        host = "localhost",
        port = 5432,
        username = "scala",
        password = "scala",
        database = "geolocation",
      )
      given GeolocationService[F] = GeolocationService()
      httpServer <- ServerResource[F]
    } yield Resources[F](
      config,
      logger,
      httpServer,
    )
}
