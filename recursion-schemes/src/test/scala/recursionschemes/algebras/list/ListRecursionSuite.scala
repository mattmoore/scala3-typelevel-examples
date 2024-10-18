package recursionschemes.algebras.list

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

    val structure = List(1, 2, 3, 4)

    expect.all(
      sum(structure) == 10,
      product(structure) == 24,
      double(structure) == List(2, 4, 6, 8),
      string(structure) == "1 :: 2 :: 3 :: 4 :: Nil",
    )
  }

  pureTest("Catamorphism with list example - all you need is a Functor and F-Algebra") {
    import higherkindness.droste.*
    import higherkindness.droste.data.*
    import higherkindness.droste.scheme.*

    val structure: List[Int] = List(1, 2, 3)
    val fixed: Fix[ListF]    = iso.forward(structure)

    val double = cata(productAlgebra)

    expect.all(
      double(fixed) == 6,
    )
  }
}
