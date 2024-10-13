package recursionschemes

import higherkindness.droste.*
import higherkindness.droste.data.*
import higherkindness.droste.syntax.all.*
import weaver.*

import ExpressionAlgebras.*

object ExpressionRecursionSuite extends SimpleIOSuite {
  pureTest("Catamorphism with expression example - all you need is a Functor and F-Algebra") {
    val exp1: Fix[Expr] = Fix(
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

    val exp2: Fix[Expr] =
      Divide(
        DecValue(5.2).fix[Expr],
        Sum(
          IntValue(10).fix[Expr],
          IntValue(5).fix[Expr],
        ).fix,
      ).fix

    val evaluate: Fix[Expr] => Double = scheme.cata(evaluateAlgebra)

    expect.all(
      evaluate(exp1) == 0.3466666666666667,
      evaluate(exp2) == 0.3466666666666667,
    )
  }
}
