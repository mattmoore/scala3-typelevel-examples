import cats.effect.IO
import fs2.Stream
import weaver.*

object IOStreamSuite extends SimpleIOSuite {
  test("Stream can be constructed with effects") {
    val eff = Stream.eval(IO { println("BEING RUN!!"); 1 + 1 })
    for {
      result <- eff.compile.toList
    } yield expect(result == List(2))
  }
}
