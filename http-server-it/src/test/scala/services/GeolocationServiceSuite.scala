package httpserver.services

import cats.effect.*
import cats.syntax.all.*
import com.dimafeng.testcontainers.PostgreSQLContainer
import httpserver.MockLogger
import httpserver.MockLogger.*
import httpserver.domain.*
import httpserver.repositories.AddressRepository
import httpserver.services.*
import natchez.Trace.Implicits.noop
import org.testcontainers.utility.DockerImageName
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.extras.LogLevel
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*
import weaver.*

import java.time.LocalDate

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
      postgresContainer <- Resource.fromAutoCloseable {
        F.blocking(postgresContainerDef.start())
      }
      addressState <- Resource.eval(F.ref(addresses))
      given Logger[F] = logger
      given AddressRepository[F] = new AddressRepository[F] {
        val session: Resource[F, Session[F]] =
          Session.single(
            host = postgresContainer.host,
            port = postgresContainer.firstMappedPort,
            user = postgresContainer.username,
            password = Some(postgresContainer.password),
            database = postgresContainer.databaseName,
          )
        override def getByAddress(address: Address): F[Option[Address]] =
          session.use { s =>
            for {
              d <- s.unique(sql"select current_date".query(date))
              result <- addressState.get.map { addresses =>
                addresses.find(_.street == address.street)
              }
            } yield result
          }

        def getQuery(address: Address): F[java.time.LocalDate] =
          session.use { s =>
            for {
              result <- s.unique(sql"select current_date".query(date))
            } yield result
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
    val session: Resource[F, Session[F]] = Session.single(
      host = r.postgresContainer.host,
      port = r.postgresContainer.firstMappedPort,
      user = r.postgresContainer.username,
      password = Some(r.postgresContainer.password),
      database = r.postgresContainer.databaseName,
    )
    for {
      logMessagesBefore <- r.logMessages.get
      result            <- r.geolocationService.getCoords(addresses.head)
      logMessagesAfter  <- r.logMessages.get
      dateResult <- session.use { s =>
        for {
          d <- s.unique(sql"select current_date".query(date))
        } yield d
      }
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
        dateResult == LocalDate.now(),
      )
    }
  }
}
