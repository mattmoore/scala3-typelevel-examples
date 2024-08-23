import cats.effect.IO
import cats.effect.kernel.Deferred
import cats.effect.unsafe.implicits.global
import cats.syntax.all.*

class LatchSuite extends munit.FunSuite {
  test("Combine Ref and Deferred to count down latches, releasing once all latches are gone") {
    trait Latch {
      def release: IO[Unit]
      def await: IO[Unit]
    }

    sealed trait State
    final case class Awaiting(latches: Int, waiter: Deferred[IO, Unit]) extends State
    case object Done                                                    extends State

    object Latch {
      def apply(latches: Int): IO[Latch] =
        for {
          waiter <- IO.deferred[Unit]
          state  <- IO.ref[State](Awaiting(latches, waiter))
        } yield new Latch {
          override def release: IO[Unit] =
            state
              .modify {
                case Awaiting(n, waiter) =>
                  if (n > 1)
                    (Awaiting(n - 1, waiter), IO.unit)
                  else
                    (Done, waiter.complete(()))
                case Done => (Done, IO.unit)
              }
              .flatten
              .void

          override def await: IO[Unit] =
            state.get.flatMap {
              case Done                => IO.unit
              case Awaiting(_, waiter) => waiter.get
            }
        }
    }

    val program: IO[Unit] =
      for {
        latch <- Latch(10)
        _ <- (1 to 10).toList.traverse { idx =>
          (IO.println(s"$idx counting down") *> latch.release).start
        }
        _ <- latch.await
        _ <- IO.println("Got past the latch")
      } yield ()

    val actual = program.unsafeRunSync()
    assertEquals(actual, ())
  }
}
