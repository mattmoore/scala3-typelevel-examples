import cats.effect.IO
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.std.Semaphore
import cats.effect.syntax.all.*
import cats.effect.unsafe.implicits.global
import cats.implicits.*

import scala.concurrent.duration.*

class SemaphoreSuite extends munit.FunSuite {
  test("Semaphore - when multiple processes try to access a resource and we want to constrain the number of accesses") {
    class PreciousResource[F[_]: Temporal](name: String, s: Semaphore[F])(implicit F: Console[F]) {
      def use: F[Unit] =
        for {
          x <- s.available
          _ <- F.println(s"$name >> Availability: $x")
          _ <- s.acquire
          y <- s.available
          _ <- F.println(s"$name >> Started | Availability: $y")
          _ <- s.release.delayBy(3.seconds)
          z <- s.available
          _ <- F.println(s"$name >> Done | Availability: $z")
        } yield ()
    }

    val program: IO[Unit] =
      for {
        s <- Semaphore[IO](1)
        r1 = new PreciousResource[IO]("R1", s)
        r2 = new PreciousResource[IO]("R2", s)
        r3 = new PreciousResource[IO]("R3", s)
        _ <- List(r1.use, r2.use, r3.use).parSequence.void
      } yield ()

    val actual = program.unsafeRunSync()
    assertEquals(actual, ())
  }
}
