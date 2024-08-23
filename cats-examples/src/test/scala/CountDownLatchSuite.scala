import cats.effect.*
import cats.effect.std.CountDownLatch
import cats.effect.unsafe.implicits.global
import cats.implicits.*

class CountDownLatchSuite extends munit.FunSuite {
  test("CountDownLatch - a one-shot concurrency primitive that blocks any fibers that wait on it") {
    val program: IO[Unit] =
      for {
        c <- CountDownLatch[IO](2)
        f <- (c.await >> IO.println("Countdown latch unblocked")).start
        _ <- c.release
        _ <- IO.println("Before latch is unblocked")
        _ <- c.release
        _ <- f.join
      } yield ()

    val actual = program.unsafeRunSync()
    assertEquals(actual, ())
  }
}
