import cats.effect.IO
import cats.effect.kernel.Deferred
import weaver.*

import scala.concurrent.duration.*

object DeferredSuite extends SimpleIOSuite {
  test("Deferred is all about blocking fibers until a condition is met") {
    def countdown(n: Int, pause: Int, waiter: Deferred[IO, Unit]): IO[Unit] =
      IO.println(n) *> IO.defer {
        if (n == 0) IO.unit
        else if (n == pause)
          IO.println("paused...")
            *> waiter.get
            *> countdown(n - 1, pause, waiter)
        else countdown(n - 1, pause, waiter)
      }

    val program: IO[Unit] =
      for {
        waiter <- IO.deferred[Unit]
        f      <- countdown(10, 5, waiter).start
        _      <- IO.sleep(5.seconds)
        _      <- waiter.complete(())
        _      <- f.join
        _      <- IO.println("blast off!")
      } yield ()

    for {
      result <- program
    } yield expect(result == ())
  }
}
