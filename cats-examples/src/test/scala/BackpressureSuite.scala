import cats.effect.IO
import cats.effect.std.Backpressure
import cats.effect.unsafe.implicits.global

import scala.concurrent.duration.*

class BackpressureSuite extends munit.FunSuite {
  test("BackpressureSuite with Lossless - running effects with rate-limiting strategy - effects will run in the presence of backpressure") {
    val program =
      for {
        backpressure <- Backpressure[IO](Backpressure.Strategy.Lossless, 1)
        f1           <- backpressure.metered(IO.sleep(1.second) *> IO.pure(1)).start
        f2           <- backpressure.metered(IO.sleep(1.second) *> IO.pure(1)).start
        res1         <- f1.joinWithNever
        res2         <- f2.joinWithNever
      } yield (res1, res2)

    val actual = program.unsafeRunSync()
    assertEquals(actual, (Option(1), Option(1)))
  }

  test("BackpressureSuite with Lossy - running effects with rate-limiting strategy") {
    val program =
      for {
        backpressure <- Backpressure[IO](Backpressure.Strategy.Lossy, 1)
        f1           <- backpressure.metered(IO.sleep(1.second) *> IO.pure(1)).start
        f2           <- backpressure.metered(IO.sleep(1.second) *> IO.pure(1)).start
        res1         <- f1.joinWithNever
        res2         <- f2.joinWithNever
      } yield (res1, res2)

    val (actual1, actual2) = program.unsafeRunSync()
    assert(
      (actual1, actual2) == (None, Option(1)) ||
        (actual1, actual2) == (Option(1), None),
    )
  }
}
