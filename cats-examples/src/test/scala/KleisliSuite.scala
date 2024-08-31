import cats.data.Kleisli
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import weaver.*

object KleisliSuite extends SimpleIOSuite {
  pureTest("Regular functions - no context (IO), ergo no Kleisli") {
    val getNumberFromDb: Unit => Int    = _ => 2
    val processNumber: Int => Int       = num => num * 2
    val writeNumberToDb: Int => Boolean = _ => true

    val combo1: Unit => Boolean = _ => writeNumberToDb(processNumber(getNumberFromDb(())))
    println(s"combo1: ${combo1(())}")

    val combo2: Unit => Boolean = writeNumberToDb compose processNumber compose getNumberFromDb
    println(s"combo2: ${combo2(())}")

    val combo3: Unit => Boolean = getNumberFromDb andThen processNumber andThen writeNumberToDb
    println(s"combo3: ${combo3(())}")

    expect.all(
      combo1(()) == true,
      combo2(()) == true,
      combo3(()) == true,
    )
  }

  test("Functions that have a context (IO) - still without Kleisli") {
    // In this case, instead of returning values, these functions return values wrapped in the IO context:
    val getNumberFromDb: Unit => IO[Int]    = _ => IO.pure(2)
    val processNumber: Int => IO[Int]       = num => IO.pure(num * 2)
    val writeNumberToDb: Int => IO[Boolean] = num => IO.pure(true)

    // We now have to chain these functions together within the context of each.
    // Here we're doing this with nested flatMaps:
    val comboFlatMap: Unit => IO[Boolean] = Unit =>
      getNumberFromDb(()) flatMap { number =>
        processNumber(number) flatMap { processed =>
          writeNumberToDb(processed)
        }
      }
    println(s"comboFlatMap: ${comboFlatMap(()).unsafeRunSync()}")

    // Nesting flatMaps is unweildy.
    // Another way to do this without nesting flatMaps is to use a for comprehension:
    val comboForComp: Unit => IO[Boolean] = Unit =>
      for {
        number    <- getNumberFromDb(())
        processed <- processNumber(number)
        result    <- writeNumberToDb(processed)
      } yield result
    println(s"comboForComp: ${comboForComp(()).unsafeRunSync()}")

    for {
      flatMapResult <- comboFlatMap(())
      forCompResult <- comboForComp(())
    } yield expect.all(
      flatMapResult == true,
      forCompResult == true,
    )
  }

  test("Kleisli lets us compose functions with a context (IO)") {
    // We've go the same functions with a context (returning IO.pure(value)):
    val getNumberFromDb: Unit => IO[Int]    = _ => IO.pure(2)
    val processNumber: Int => IO[Int]       = num => IO.pure(num * 2)
    val writeNumberToDb: Int => IO[Boolean] = num => IO.pure(true)

    // We want a way to compose these functions.
    // In the first test scenario without IO, we were able to use `compose` or `andThen`.
    // But once we add IO to the mix, we could no longer compose those functions.
    // This is the whole point of Kleisli - to compose functions that have a context (wrapped in IO).

    // In order to use Kleisli compositiion, we have to "lift" the functions into the Kleisli.
    // There's a couple ways to do this.
    // The first method is to define new functions that lift into Kleisli:
    val getNumberFromDbK: Kleisli[IO, Unit, Int]    = Kleisli(getNumberFromDb)
    val processNumberK: Kleisli[IO, Int, Int]       = Kleisli(processNumber)
    val writeNumberToDbK: Kleisli[IO, Int, Boolean] = Kleisli(writeNumberToDb)
    val comboKleisli1: Kleisli[IO, Unit, Boolean] =
      getNumberFromDbK andThen
        processNumberK andThen
        writeNumberToDbK
    println(s"comboKleisli1: ${comboKleisli1(())}")

    // Defining new Kleisli functions to lift the original IO functions is extra code and a bit tedious.
    // There's another way to do this that's much nicer.
    // cats provides an auto-apply baked into `andThen` that we can rely on.
    // In order to use this built-in behavior, we do still have to lift the first function into Kleisli.
    // But after that the rest just works:
    val comboKleisli2: Kleisli[IO, Unit, Boolean] =
      Kleisli(getNumberFromDb) andThen
        processNumber andThen
        writeNumberToDb

    println(s"comboKleisli2: ${comboKleisli2(())}")

    for {
      kleisli1 <- comboKleisli1(())
      kleisli2 <- comboKleisli2(())
    } yield expect.all(
      kleisli1 == true,
      kleisli2 == true,
    )
  }
}
