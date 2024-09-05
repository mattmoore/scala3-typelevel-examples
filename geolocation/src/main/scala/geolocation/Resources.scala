package geolocation

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import fs2.io.net.Network
import geolocation.http.ServerResource
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import natchez.Trace.Implicits.noop
import org.http4s.server.Server
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import skunk.Session

import domain.*
import repositories.*

final case class Resources[F[_]](
    config: Config,
    dbSession: Resource[F, Session[F]],
    addressRepo: AddressRepository[F],
    helloService: HelloService[F],
    geolocationService: GeolocationService[F],
    httpServer: Server,
)

object Resources {
  def make[F[_]: Async: Console: Network]: Resource[F, Resources[F]] =
    for {
      config <- Resource.eval(Config.load[F])
      given SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F](
        name = LoggerName("geolocation"),
      )
      _ <- Migrations.migrate(config.databaseConfig)
      session <- Session.pooled(
        host = config.databaseConfig.host,
        port = config.databaseConfig.port,
        user = config.databaseConfig.username,
        password = Some(config.databaseConfig.password),
        database = config.databaseConfig.database,
        max = config.databaseConfig.maxConnections,
      )
      addressRepo: AddressRepository[F]         = AddressRepository(config, session)
      helloService: HelloService[F]             = HelloService.apply
      geolocationService: GeolocationService[F] = GeolocationService(addressRepo)
      httpServer: Server <- ServerResource.make[F](config, helloService, geolocationService)
    } yield {
      Resources[F](
        config,
        session,
        addressRepo,
        helloService,
        geolocationService,
        httpServer,
      )
    }
}
