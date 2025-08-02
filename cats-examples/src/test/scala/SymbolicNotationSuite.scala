import cats.syntax.all.*
import cats.effect.*
import weaver.*

object SymbolicNotationSuite extends SimpleIOSuite {
  test("flatMap with >>=") {
    for {
      result <- IO.pure(1) >>= (x => IO(x * 2))
    } yield expect(result == 2)
  }
}
