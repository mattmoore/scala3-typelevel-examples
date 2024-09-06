import cats.*
import cats.effect.*
import cats.syntax.all.*
import weaver.*

object ParallelSuite extends SimpleIOSuite {
  test("parBisequence") {
    val a = IO.pure(1)
    val b = IO.pure(2)

    for {
      result <- (a, b).parBisequence
    } yield expect.all(
      result == (1, 2),
    )
  }
}
