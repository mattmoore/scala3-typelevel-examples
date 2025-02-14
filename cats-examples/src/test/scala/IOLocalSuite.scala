import weaver.*
import cats.effect.*

object IOLocalSuite extends SimpleIOSuite {
  test("IOLocal allows us to embed a context for IO operations") {

    def add(x: Int, y: Int)(using local: IOLocal[Int]): IO[Int] =
      IO(x + y).flatTap { result =>
        local.update(_ + result)
      }

    for {
      local <- IOLocal(0)
      given IOLocal[Int] = local
      result       <- add(1, 1)
      result2      <- add(1, 1)
      result3      <- add(1, 1)
      updatedLocal <- local.get
    } yield expect.all(
      result == 2,
      result2 == 2,
      result3 == 2,
      updatedLocal == 6,
    )
  }
}
