package recursionschemes.algebras.list

import cats.syntax.all.*
import weaver.*

object ListRecursionSuite extends SimpleIOSuite {
  pureTest("Catamorphism with list example - full droste example, using Scala List") {
    import higherkindness.droste.*
    import higherkindness.droste.data.list.*
    import higherkindness.droste.scheme.*

    val sumAlgebra: Algebra[ListF[Int, *], Int] = Algebra {
      case NilF              => 0
      case ConsF(head, tail) => head + tail
    }

    val productAlgebra: Algebra[ListF[Int, *], Int] = Algebra {
      case NilF              => 1
      case ConsF(head, tail) => head * tail
    }

    val doubleAlgebra: Algebra[ListF[Int, *], List[Int]] = Algebra {
      case NilF              => List.empty
      case ConsF(head, tail) => head * 2 :: tail
    }

    val stringAlgebra: Algebra[ListF[Int, *], String] = Algebra {
      case NilF              => "Nil"
      case ConsF(head, tail) => s"$head :: $tail"
    }

    val sum     = cata(sumAlgebra)
    val product = cata(productAlgebra)
    val double  = cata(doubleAlgebra)
    val string  = cata(stringAlgebra)

    val list = List(1, 2, 3, 4)

    expect.all(
      sum(list) == 10,
      product(list) == 24,
      double(list) == List(2, 4, 6, 8),
      string(list) == "1 :: 2 :: 3 :: 4 :: Nil",
    )
  }

  pureTest("Catamorphism with list example - all you need is a Functor and F-Algebra") {
    import higherkindness.droste.scheme.*

    val list: List[Int] = List(1, 2, 3, 4)

    lazy val evaluateSum: List[Int] => Int =
      iso.forward >>> cata(sumAlgebra)

    lazy val evaluateProduct: List[Int] => Int =
      iso.forward >>> cata(productAlgebra)

    lazy val evaluateDouble: List[Int] => List[Int] =
      iso.forward >>> cata(doubleAlgebra)

    lazy val evaluateString: List[Int] => String =
      iso.forward >>> cata(stringAlgebra)

    expect.all(
      evaluateSum(list) == 10,
      evaluateProduct(list) == 24,
      evaluateDouble(list) == List(2, 4, 6, 8),
      evaluateString(list) == "1 :: 2 :: 3 :: 4 :: Nil",
    )
  }
}
