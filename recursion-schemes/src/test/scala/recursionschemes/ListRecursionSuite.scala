package recursionschemes

import weaver.*

object ListRecursionSuite extends SimpleIOSuite {
  pureTest("Catamorphism with list example - all you need is a Functor and F-Algebra") {
    import ListAlgebras.*
    import higherkindness.droste.*
    import higherkindness.droste.data.*

    val structure = Fix(ConsF(1, Fix(ConsF(2, Fix(ConsF(3, Fix(NilF)))))))

    expect.all(
      scheme.cata(doubleAlgebra).apply(structure) == 6,
    )
  }

  pureTest("Catamorphism with list example - fully using droste") {
    import higherkindness.droste.*
    import higherkindness.droste.data.list.*

    val doubleAlgebra: Algebra[ListF[Int, *], Int] = Algebra {
      case NilF              => 1
      case ConsF(head, tail) => head * tail
    }

    val data = List(1, 2, 3)

    expect.all(
      scheme.cata(doubleAlgebra).apply(data) == 6,
    )
  }
}
