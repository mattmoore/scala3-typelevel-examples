import cats.effect.IO
import cats.effect.std.Supervisor
import weaver.*

import scala.concurrent.duration.*

object SupervisorSuite extends SimpleIOSuite {
  test("Supervisor - spawn a fiber that outlives the scope that created it, but we can still control its lifecycle") {
    val thingToDo: IO[Unit] =
      IO.sleep(3.seconds)

    for {
      result <- Supervisor[IO](await = true).use { supervisor =>
        supervisor.supervise(thingToDo).void
      }
    } yield expect(result == ())
  }
}
