package geolocation.services

import cats.effect.*
import cats.syntax.all.*
import geolocation.MockLogger
import geolocation.MockLogger.LogMessage
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.extras.LogLevel
import weaver.*

object HelloServiceSuite extends SimpleIOSuite {
  private type F[A] = IO[A]

  test("hello returns a greeting with the name interpolated") {
    for {
      logMessages <- Ref[F].of(List.empty[LogMessage])
      given Logger[F] = MockLogger[F](logMessages)
      helloService    = HelloService[F]()

      logMessagesBefore <- logMessages.get
      result            <- helloService.hello("Matt")
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
