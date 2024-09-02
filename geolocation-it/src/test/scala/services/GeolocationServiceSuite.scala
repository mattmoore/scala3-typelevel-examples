package geolocation.services

import cats.effect.*
import cats.syntax.all.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import containers.PostgresContainer
import geolocation.MockLogger
import geolocation.MockLogger.*
import geolocation.domain.*
import geolocation.repositories.AddressRepository
import geolocation.services.*
import natchez.Trace.Implicits.noop
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.extras.LogLevel
import weaver.*

object GeolocationServiceSuite extends IOSuite {
  private type F[A] = IO[A]
  private val F = Async[F]

  final case class TestResource(
      logMessages: Ref[F, List[LogMessage]],
      logger: Logger[F],
      geolocationService: GeolocationService[F],
      postgresContainer: PostgreSQLContainer,
  )
  override final type Res = TestResource
  override final val sharedResource: Resource[F, Res] =
    for {
      logMessages <- Resource.eval(F.ref(List.empty[LogMessage]))
      logger               = MockLogger[F](logMessages)
      postgresContainerDef = PostgresContainer()
      postgresContainer <- Resource.fromAutoCloseable {
        F.blocking(postgresContainerDef.start())
      }
      given Config = Config(
        port = 5432,
        databaseConfig = DatabaseConfig(
          host = postgresContainer.host,
          port = postgresContainer.firstMappedPort,
          username = postgresContainer.username,
          password = postgresContainer.password,
          database = postgresContainer.databaseName,
        ),
      )
      given Logger[F]            = logger
      given AddressRepository[F] = AddressRepository()
      geolocationService         = GeolocationService[F]()
    } yield TestResource(
      logMessages,
      logger,
      geolocationService,
      postgresContainer,
    )

  test("getCoords returns GPS coordinates for a given address") { r =>
    for {
      logMessagesBefore <- r.logMessages.get
      result <- r.geolocationService.getCoords(
        AddressQuery(
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
        ),
      )
      logMessagesAfter <- r.logMessages.get
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