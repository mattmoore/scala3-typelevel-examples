import cats.effect.Async
import cats.effect.IO
import cats.effect.Ref
import cats.syntax.all.*
import weaver.*

object RefSuite extends SimpleIOSuite {
  test("Simple Ref example") {
    for {
      stateRef <- IO.ref(0)
      state1   <- stateRef.get
      _        <- stateRef.update(x => x + 1)
      state2   <- stateRef.get
    } yield expect.all(
      state2 == 1,
    )
  }

  test("Ref is all about atomic concurrent operations") {
    for {
      state  <- IO.ref(0)
      fibers <- state.update(_ + 1).start.replicateA(100)
      _      <- fibers.traverse(_.join).void
      value  <- state.get
      _      <- IO.println(s"The final value is $value")
    } yield expect.all(
      value == 100,
    )
  }

  test("Ref can be used to stub database tables in unit tests") {
    case class Person(id: Int, name: String)
    case class PersonRow(id: Int, name: String)

    trait Repository[F[_], A] {
      def get(id: Int): F[Option[A]]
      def put(row: A): F[Unit]
    }

    trait PersonService[F[_]] {
      def get(id: Int): F[Option[Person]]
      def put(person: Person): F[Unit]
    }

    object StubbedRepository {
      def apply[F[_]: Async](state: Ref[F, List[PersonRow]]): Repository[F, PersonRow] =
        new Repository[F, PersonRow] {
          override def get(id: Int): F[Option[PersonRow]] =
            state.get.map(ps => ps.find(_.id == id))

          override def put(person: PersonRow): F[Unit] =
            state.update(ps => person +: ps)
        }
    }

    object PersonService {
      def apply[F[_]: Async](repo: Repository[F, PersonRow]): PersonService[F] =
        new PersonService[F] {
          override def get(id: Int): F[Option[Person]] =
            for {
              row <- repo.get(id)
            } yield row.map(row => Person(row.id, row.name))

          override def put(person: Person): F[Unit] =
            repo.put(PersonRow(person.id, person.name))
        }
    }

    for {
      tableState <- Ref[IO].of(List.empty[PersonRow])
      repo          = StubbedRepository(tableState)
      personService = PersonService(repo)
      rowsBefore <- tableState.get
      _          <- personService.put(Person(1, "Matt"))
      rowsAfter  <- tableState.get
    } yield expect.all(
      rowsBefore == List.empty,
      rowsAfter == List(PersonRow(1, "Matt")),
    )
  }
}
