package recursionschemes

import higherkindness.droste.*
import higherkindness.droste.data.*
import weaver.*

import Expression.*

object ExpressionRecursionSuite extends SimpleIOSuite {
  pureTest("Catamorphism with expression example - all you need is a Functor and F-Algebra") {
    val exp2: Fix[Exp] = Fix(
      Divide(
        Fix(DecValue(5.2)),
        Fix(
          Sum(
            Fix(IntValue(10)),
            Fix(IntValue(5)),
          ),
        ),
      ),
    )

    expect.all(
      scheme.cata(evaluate).apply(exp2) == 0.3466666666666667,
    )
  }
}
