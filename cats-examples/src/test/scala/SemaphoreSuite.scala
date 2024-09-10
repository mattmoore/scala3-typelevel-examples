import cats.effect.*
import cats.effect.std.*
import cats.effect.syntax.all.*
import cats.implicits.*
import weaver.*

import scala.concurrent.duration.*

object SemaphoreSuite extends SimpleIOSuite {
  type F[A] = IO[A]

  test("From cats effect website example: Semaphore is useful for throttling - when multiple processes try to access a resource and we want to constrain the number of accesses") {
    class PreciousResource[F[_]: Temporal](name: String, s: Semaphore[F])(using F: Console[F]) {
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

    for {
      s <- Semaphore[IO](1)
      r1 = new PreciousResource[IO]("R1", s)
      r2 = new PreciousResource[IO]("R2", s)
      r3 = new PreciousResource[IO]("R3", s)
      result <- List(r1.use, r2.use, r3.use).parSequence.void
    } yield expect(result == ())
  }

  test("Semaphore example with parTraverse") {
    case class PersonRow(id: Int, firstName: String, lastName: String)

    final class DataRepository[F[_]: Temporal](store: AtomicCell[F, List[PersonRow]], s: Semaphore[F])(using F: Console[F]) {
      def putPerson(person: PersonRow): F[Unit] =
        for {
          _ <- s.acquire
          x <- s.available
          _ <- F.println(s"DataRepository availability: $x")
          _ <- store.update(person +: _)
          _ <- s.release
          y <- s.available
          _ <- F.println(s"DataRepository availability: $y")
        } yield ()
    }

    val people = List.tabulate(10)(n => PersonRow(n + 1, "Matt", "Moore"))

    // To demonstrate that in spite of calling parTraverseN, we're limiting to 1 worker thread, so only one fiber gets executed at a time
    val WorkerThreadCount = 1

    for {
      table     <- AtomicCell[F].of(List.empty[PersonRow])
      semaphore <- Semaphore[F](WorkerThreadCount)
      repo = new DataRepository(table, semaphore)
      rowsBefore <- table.get
      _          <- people.parTraverse(repo.putPerson).void
      rowsAfter  <- table.get
    } yield expect.all(
      rowsBefore == List.empty,
      rowsAfter.sortBy(_.id) == people,
    )
  }

  test("Semaphore example with parTraverseN - parTraverseN automatically utilizes a semaphore, so we don't have to handle it") {
    case class PersonRow(id: Int, firstName: String, lastName: String)

    final class DataRepository[F[_]: Temporal](store: AtomicCell[F, List[PersonRow]]) {
      def putPerson(person: PersonRow): F[Unit] =
        store.update(person +: _)
    }

    val people = List.tabulate(10)(n => PersonRow(n + 1, "Matt", "Moore"))

    // To demonstrate that in spite of calling parTraverseN, we're limiting to 1 worker thread, so only one fiber gets executed at a time
    val WorkerThreadCount = 1

    for {
      table <- AtomicCell[F].of(List.empty[PersonRow])
      repo = new DataRepository[F](table)
      rowsBefore <- table.get
      _          <- people.parTraverseN(WorkerThreadCount)(repo.putPerson).void
      rowsAfter  <- table.get
    } yield expect.all(
      rowsBefore == List.empty,
      rowsAfter.sortBy(_.id) == people,
    )
  }
}
