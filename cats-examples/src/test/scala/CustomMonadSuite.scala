import cats.Monad
import weaver.*

import scala.annotation.tailrec

object CustomMonadSuite extends SimpleIOSuite {
  case class CustomMonad[A](value: A)

  implicit val customMonad: Monad[CustomMonad] = new Monad[CustomMonad] {
    override def pure[A](x: A): CustomMonad[A] =
      CustomMonad(x)

    override def flatMap[A, B](fa: CustomMonad[A])(f: A => CustomMonad[B]): CustomMonad[B] =
      f.apply(fa.value)

    @tailrec
    override def tailRecM[A, B](a: A)(f: A => CustomMonad[Either[A, B]]): CustomMonad[B] =
      f(a) match {
        case CustomMonad(either) =>
          either match {
            case Left(a)  => tailRecM(a)(f)
            case Right(b) => CustomMonad(b)
          }
      }
  }

  pureTest("CustomMonad is a monad and can compose as long as it implements pure, flatMap and tailRecM") {
    import cats.implicits.*

    val program = for {
      a <- CustomMonad(1)
      b <- CustomMonad(2)
    } yield a + b

    expect(program == CustomMonad(3))
  }
}
