import cats.arrow.Arrow
import cats.syntax.all.*
import weaver.*

object ArrowSuite extends SimpleIOSuite {
  pureTest("Regular function composition") {
    def addOne(x: Int): Int =
      x + 1

    def double(x: Int): Int =
      x * 2

    def addAndDouble: Int => Int =
      double compose addOne

    expect.all(
      addOne(1) == 2,
      double(1) == 2,
      addAndDouble(1) == 4,
    )
  }

  pureTest("Arrow with Arrow.lift") {
    def addOne(x: Int): Int =
      x + 1

    def double(x: Int): Int =
      x * 2

    val addOneArrow       = Arrow[Function1].lift(addOne)
    val doubleArrow       = Arrow[Function1].lift(double)
    val addOneDoubleArrow = addOneArrow >>> doubleArrow

    expect.all(
      addOne(1) == 2,
      double(1) == 2,
      addOneArrow(1) == 2,
      doubleArrow(1) == 2,
      addOneDoubleArrow(1) == 4,
    )
  }

  pureTest("Function composition with >>> but not requiring explicit lifting") {
    def addOne(x: Int): Int =
      x + 1

    def double(x: Int): Int =
      x * 2

    def addOneDouble: Int => Int =
      addOne >>> double

    expect.all(
      addOne(1) == 2,
      double(1) == 2,
      addOneDouble(1) == 4,
    )
  }
}
