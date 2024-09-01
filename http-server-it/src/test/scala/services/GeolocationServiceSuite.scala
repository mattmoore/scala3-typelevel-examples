package httpserver.services

import cats.effect.*
import cats.syntax.all.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import containers.PostgresContainer
import httpserver.MockLogger
import httpserver.MockLogger.*
import httpserver.domain.*
import httpserver.repositories.AddressRepository
import httpserver.services.*
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
      given Logger[F] = logger
      given AddressRepository[F] = AddressRepository(
        host = postgresContainer.host,
        port = postgresContainer.firstMappedPort,
        username = postgresContainer.username,
        password = postgresContainer.password,
        database = postgresContainer.databaseName,
      )
      geolocationService = GeolocationService[F]()
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
        Address(
          id = 1,
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
          coords = GpsCoords(40.748643670602384, -73.98570731665924),
        ),
      )
      logMessagesAfter <- r.logMessages.get
    } yield {
      expect.all(
        result == GpsCoords(40.748643670602384, -73.98570731665924).some,
        logMessagesBefore.size == 0,
        logMessagesAfter.size == 1,
        logMessagesAfter == List(
          LogMessage(
            LogLevel.Info,
            "Invoked getCoords(Address(1,20 W 34th St.,New York,NY,GpsCoords(40.748643670602384,-73.98570731665924)))",
          ),
        ),
      )
    }
  }
}
