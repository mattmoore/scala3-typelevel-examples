import cats.effect.*
import cats.effect.std.Random

import scala.concurrent.duration.*

def raceTest[A](effects: List[IO[A]]): IO[A] =
  effects.reduceLeft((io1, io2) =>
    IO.race(io1, io2)
      .map(_.fold(identity, identity)),
  )

def raceTest2[A](effects: List[IO[A]]): IO[A] =
  effects.reduce(_.race(_).map(_.merge))

def expensiveThing(n: Int, rand: Random[IO]): IO[Option[Int]] =
  for {
    delay  <- rand.betweenInt(2, 4)
    _      <- IO.sleep(delay.seconds)
    result <- if n == 49 then IO.pure(Some(n)) else IO.pure(None)
  } yield result

object RaceManyDemo extends IOApp.Simple {
  override def run: IO[Unit] =
    for {
      rand <- Random.scalaUtilRandom[IO]
      effects = List.tabulate(50)(n => expensiveThing(n, rand))
      // reduceLeftRace current
      reduceLeftRaceStart = System.nanoTime()
      reduceLeftRaceResult: Option[Int] <- raceTest(effects)
      _                                 <- IO.println(s"reduceLeftRace TIME: ${(System.nanoTime() - reduceLeftRaceStart) / 1000 / 1000 / 10}")
      _                                 <- IO.println(s"reduceLeftRace VALUE: $reduceLeftRaceResult")
      // "raceMany" via reduce, map, merge
      raceManyStart = System.nanoTime()
      raceTest2Result: Option[Int] <- raceTest2(effects)
      _                            <- IO.println(s"raceMany TIME: ${(System.nanoTime() - raceManyStart) / 1000 / 1000 / 10}")
      _                            <- IO.println(s"raceMany VALUE: $raceTest2Result")
    } yield ()
}
