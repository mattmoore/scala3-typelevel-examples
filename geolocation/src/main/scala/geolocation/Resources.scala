package geolocation

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import fs2.io.net.Network
import natchez.Trace.Implicits.noop
import org.typelevel.log4cats.*
import org.typelevel.log4cats.slf4j.*
import skunk.Session

import domain.*
import repositories.*

final case class Resources[F[_]](
    config: Config,
    logger: SelfAwareStructuredLogger[F],
    dbSession: Resource[F, Session[F]],
    addressRepo: AddressRepository[F],
)

object Resources {
  def make[F[_]: Async: Console: Network]: Resource[F, Resources[F]] =
    for {
      config <- Resource.eval(Config.load[F])
      given Config = config
      session <- Session.pooled(
        host = config.databaseConfig.host,
        port = config.databaseConfig.port,
        user = config.databaseConfig.username,
        password = Some(config.databaseConfig.password),
        database = config.databaseConfig.database,
        max = 10,
      )
      given Resource[F, Session[F]]        = session
      given LoggerFactory[F]               = Slf4jFactory.create[F]
      logger: SelfAwareStructuredLogger[F] = LoggerFactory[F].getLogger
      addressRepo: AddressRepository[F]    = AddressRepository[F]()
    } yield {
      Resources[F](
        config,
        logger,
        session,
        addressRepo,
      )
    }
}
