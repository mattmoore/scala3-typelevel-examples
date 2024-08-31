package httpserver.services

import cats.effect.*
import cats.syntax.all.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import httpserver.MockLogger
import httpserver.MockLogger.*
import httpserver.domain.*
import httpserver.repositories.AddressRepository
import httpserver.services.*
import org.testcontainers.utility.DockerImageName
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

  val addresses = List(
    Address(
      street = "123 Anywhere St.",
      city = "Anywhere",
      state = "MI",
      coords = GpsCoords(10, 10),
    ),
  )

  override final type Res = TestResource
  override final val sharedResource: Resource[F, Res] =
    for {
      logMessages <- Resource.eval(F.ref(List.empty[LogMessage]))
      logger = MockLogger[F](logMessages)
      postgresContainerDef = PostgreSQLContainer.Def(
        dockerImageName = DockerImageName.parse("postgres:latest"),
        databaseName = "testcontainer-scala",
        username = "scala",
        password = "password",
      )
      postgresContainer <- Resource.eval {
        F.blocking(postgresContainerDef.start())
      }
      addressState <- Resource.eval(F.ref(addresses))
      given Logger[F] = logger
      given AddressRepository[F] = new AddressRepository[F] {
        override def getByAddress(address: Address): F[Option[Address]] =
          addressState.get.map { addresses =>
            addresses.find(_.street == address.street)
          }
      }
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
      result            <- r.geolocationService.getCoords(addresses.head)
      logMessagesAfter  <- r.logMessages.get
    } yield {
      expect.all(
        result == GpsCoords(10, 10).some,
        logMessagesBefore.size == 0,
        logMessagesAfter.size == 1,
        logMessagesAfter == List(
          LogMessage(
            LogLevel.Info,
            "Invoked getCoords(Address(123 Anywhere St.,Anywhere,MI,GpsCoords(10.0,10.0)))",
          ),
        ),
      )
    }
  }
}
