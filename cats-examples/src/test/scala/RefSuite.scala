import cats.effect.IO
import cats.syntax.all.*
import cats.effect.unsafe.implicits.global

class RefSuite extends munit.FunSuite {
  test("Ref is all about atomic concurrent operations") {
    val program: IO[Int] =
      for {
        state  <- IO.ref(0)
        fibers <- state.update(_ + 1).start.replicateA(100)
        _      <- fibers.traverse(_.join).void
        value  <- state.get
        _      <- IO.println(s"The final value is $value")
      } yield value

    val actual = program.unsafeRunSync()
    assertEquals(actual, 100)
  }
}
