package geolocation.it.services

import cats.effect.*
import cats.syntax.all.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import geolocation.domain.*
import geolocation.it.MockLogger
import geolocation.it.MockLogger.*
import geolocation.it.containers.PostgresContainer
import geolocation.repositories.AddressRepository
import geolocation.services.*
import natchez.Trace.Implicits.noop
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.extras.LogLevel
import skunk.Session
import weaver.*
import geolocation.Migrations

object GeolocationServiceSuite extends IOSuite {
  private type F[A] = IO[A]
  private val F = Async[F]

  final case class TestResource(
      config: Config,
      postgresContainer: PostgreSQLContainer,
  )
  override final type Res = TestResource
  override final val sharedResource: Resource[F, Res] =
    for {
      postgresContainer <- Resource.fromAutoCloseable(F.delay(PostgresContainer().start()))
      config = Config(
        port = 5432,
        databaseConfig = DatabaseConfig(
          host = postgresContainer.host,
          port = postgresContainer.firstMappedPort,
          username = postgresContainer.username,
          password = postgresContainer.password,
          database = postgresContainer.databaseName,
          migrationsLocation = "filesystem:../geolocation/db",
        ),
      )
      _ <- Migrations.migrate(config.databaseConfig)
    } yield TestResource(
      config,
      postgresContainer,
    )

  private def pooledSessionR(config: Config): Resource[F, Resource[F, Session[F]]] =
    Session
      .pooled(
        host = config.databaseConfig.host,
        port = config.databaseConfig.port,
        user = config.databaseConfig.username,
        password = Some(config.databaseConfig.password),
        database = config.databaseConfig.database,
        max = 1,
      )

  test("getCoords returns GPS coordinates for a given address") { r =>
    pooledSessionR(r.config).use { session =>
      for {
        logMessages <- F.ref(List.empty[LogMessage])
        logger                            = MockLogger[F](logMessages)
        given Config                      = r.config
        given Logger[F]                   = logger
        addressRepo: AddressRepository[F] = AddressRepository(r.config, session)
        geolocationService                = GeolocationService[F](addressRepo)
        query = AddressQuery(
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
        )

        logMessagesBefore <- logMessages.get
        result            <- geolocationService.getCoords(query)
        logMessagesAfter  <- logMessages.get
      } yield {
        expect.all(
          result == Right(GpsCoords(40.689247, -74.044502)),
          logMessagesBefore.size == 0,
          logMessagesAfter.size == 1,
          logMessagesAfter == List(
            LogMessage(
              LogLevel.Info,
              "Invoked getCoords(AddressQuery(20 W 34th St.,New York,NY))",
            ),
          ),
        )
      }
    }
  }

  test("create stores a new address") { r =>
    pooledSessionR(r.config).use { session =>
      for {
        logMessages <- F.ref(List.empty[LogMessage])
        logger                            = MockLogger[F](logMessages)
        given Config                      = r.config
        given Logger[F]                   = logger
        addressRepo: AddressRepository[F] = AddressRepository(r.config, session)
        geolocationService                = GeolocationService[F](addressRepo)
        newAddress = Address(
          id = 3,
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
          GpsCoords(40.689247, -74.044502),
        )

        logMessagesBefore <- logMessages.get
        result            <- geolocationService.create(newAddress)
        logMessagesAfter  <- logMessages.get
      } yield {
        expect.all(
          result == Right(()),
          logMessagesBefore.size == 0,
          logMessagesAfter.size == 1,
          logMessagesAfter == List(
            LogMessage(
              LogLevel.Info,
              "Invoked create(Address(3,20 W 34th St.,New York,NY,GpsCoords(40.689247,-74.044502)))",
            ),
          ),
        )
      }
    }
  }
}
