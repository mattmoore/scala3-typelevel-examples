package recursionschemes.algebras.simpleexpression

import higherkindness.droste.*
import higherkindness.droste.data.*
import higherkindness.droste.scheme.*
import recursionschemes.algebras.simpleexpression.*
import weaver.*

object ExpressionRecursionSuite extends SimpleIOSuite {
  pureTest("Catamorphism expression example - explicit fix") {
    val expression: Fix[ExpressionF] = Fix(
      DivideF(
        Fix(DecValueF(5.2)),
        Fix(
          SumF(
            Fix(IntValueF(10)),
            Fix(IntValueF(5)),
          ),
        ),
      ),
    )

    val compile: Fix[ExpressionF] => Double =
      cata(expressionAlgebra)

    expect.all(
      compile(expression) == 0.3466666666666667,
    )
  }

  pureTest("Catamorphism expression example - embed the expression within the fix point using an isomorphism") {
    val expression: Expression =
      Divide(
        DecValue(5.2),
        Sum(
          IntValue(10),
          IntValue(5),
        ),
      )

    val expressionF: Fix[ExpressionF] =
      iso.forward(expression)

    val compile: Fix[ExpressionF] => Double =
      cata(expressionAlgebra)

    expect.all(
      compile(expressionF) == 0.3466666666666667,
    )
  }
}
