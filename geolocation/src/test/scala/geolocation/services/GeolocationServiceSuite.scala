package geolocation.services

import cats.effect.*
import cats.syntax.all.*
import geolocation.MockLogger
import geolocation.MockLogger.LogMessage
import geolocation.domain.*
import geolocation.repositories.*
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
          street = "20 W 34th St.",
          city = "New York",
          state = "NY",
          coords = GpsCoords(40.689247, -74.044502),
        ),
      )
      addressState <- F.ref(addresses)
      given AddressRepository[F] = new AddressRepository[F] {
        override def getByAddress(query: AddressQuery): F[Option[Address]] =
          addressState.get.map { addresses =>
            addresses.find(_.street == query.street)
          }
      }
      geolocationService = GeolocationService[F]()

      addressQuery = AddressQuery(
        street = addresses.head.street,
        city = addresses.head.city,
        state = addresses.head.state,
      )

      logMessagesBefore <- logMessages.get
      result            <- geolocationService.getCoords(addressQuery)
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
