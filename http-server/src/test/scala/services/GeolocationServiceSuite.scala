package httpserver.services

import cats.effect.*
import cats.syntax.all.*
import httpserver.MockLogger
import httpserver.MockLogger.LogMessage
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.extras.LogLevel
import weaver.*

object GeolocationServiceSuite extends SimpleIOSuite {
  test("Test 1") {
    for {
      logMessages <- Ref[IO].of(List.empty[LogMessage])
      given Logger[IO]   = MockLogger[IO](logMessages)
      geolocationService = GeolocationService[IO]()

      logMessagesBefore <- logMessages.get
      result            <- geolocationService.hello("Matt")
      logMessagesAfter  <- logMessages.get
    } yield {
      expect.all(
        result == "Hello, Matt.",
        logMessagesBefore.size == 0,
        logMessagesAfter.size == 1,
        logMessagesAfter == List(LogMessage(LogLevel.Info, "Invoked hello(Matt)")),
        logMessagesAfter.head.level == LogLevel.Info,
        logMessagesAfter.head.message == "Invoked hello(Matt)",
      )
    }
  }
}
