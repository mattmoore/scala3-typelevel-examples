package httpserver.services

import cats.effect.*
import cats.syntax.all.*
import httpserver.MockLogger
import httpserver.MockLogger.LogMessage
import httpserver.domain.*
import httpserver.repositories.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.extras.LogLevel
import weaver.*

object GeolocationServiceSuite extends SimpleIOSuite {
  private type F[A] = IO[A]
  private val F = Async[F]

  test("getCoords returns GPS coordinates for a given address") {
    for {
      logMessages <- F.ref(List.empty[LogMessage])
      given Logger[F] = MockLogger[F](logMessages)

      addresses = List(
        Address(
          id = 1,
          street = "123 Anywhere St.",
          city = "Anywhere",
          state = "MI",
          coords = GpsCoords(10, 10),
        ),
      )
      addressState <- F.ref(addresses)
      given AddressRepository[F] = new AddressRepository[F] {
        override def getByAddress(address: Address): F[Option[Address]] =
          addressState.get.map { addresses =>
            addresses.find(_.street == address.street)
          }
      }
      geolocationService = GeolocationService[F]()

      logMessagesBefore <- logMessages.get
      result            <- geolocationService.getCoords(addresses.head)
      logMessagesAfter  <- logMessages.get
    } yield {
      expect.all(
        result == Right(GpsCoords(10, 10)),
        logMessagesBefore.size == 0,
        logMessagesAfter.size == 1,
        logMessagesAfter == List(
          LogMessage(
            LogLevel.Info,
            "Invoked getCoords(Address(1,123 Anywhere St.,Anywhere,MI,GpsCoords(10.0,10.0)))",
          ),
        ),
      )
    }
  }
}
