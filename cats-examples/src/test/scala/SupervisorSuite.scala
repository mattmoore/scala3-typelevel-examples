import cats.effect.IO
import cats.effect.std.Supervisor
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration.*

class SupervisorSuite extends munit.FunSuite {
  test("Supervisor - spawn a fiber that outlives the scope that created it, but we can still control its lifecycle") {
    val thingToDo: IO[Unit] =
      IO.sleep(3.seconds)

    val program: IO[Unit] =
      Supervisor[IO](await = true).use { supervisor =>
        supervisor.supervise(thingToDo).void
      }

    val actual = program.unsafeRunSync()
    assertEquals(actual, ())
  }
}
