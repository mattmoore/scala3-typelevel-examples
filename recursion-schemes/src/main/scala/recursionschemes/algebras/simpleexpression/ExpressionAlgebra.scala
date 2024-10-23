package recursionschemes.algebras.simpleexpression

import cats.*
import higherkindness.droste.*
import higherkindness.droste.data.*
import recursionschemes.algebras.*

/** An isomorphism allows us to express the same structure in two different forms while allowing us to go back and forth between both forms without losing any information.
  */
lazy val iso: IsoSet[Expression, Fix[ExpressionF]] =
  new (Expression <=> Fix[ExpressionF]) {
    def forward: Expression => Fix[ExpressionF] = {
      case IntValue(v)                            => Fix(IntValueF(v))
      case DecValue(v)                            => Fix(DecValueF(v))
      case Sum(x: Expression, y: Expression)      => Fix(SumF(forward(x), forward(y)))
      case Multiply(x: Expression, y: Expression) => Fix(MultiplyF(forward(x), forward(y)))
      case Divide(x: Expression, y: Expression)   => Fix(DivideF(forward(x), forward(y)))
      case Square(x: Expression)                  => Fix(SquareF(forward(x)))
    }

    def inverse: Fix[ExpressionF] => Expression = {
      case Fix(IntValueF(v))    => IntValue(v)
      case Fix(DecValueF(v))    => DecValue(v)
      case Fix(SumF(x, y))      => Sum(inverse(x), inverse(y))
      case Fix(MultiplyF(x, y)) => Multiply(inverse(x), inverse(y))
      case Fix(DivideF(x, y))   => Divide(inverse(x), inverse(y))
      case Fix(SquareF(x))      => Square(inverse(x))
    }
  }

/** Functor defines how to map over recursive expression types. Think of this as the instructions for navigating the expression tree.
  */
implicit val expressionFFunctor: Functor[ExpressionF] =
  new Functor[ExpressionF] {
    def map[A, B](exp: ExpressionF[A])(f: A => B): ExpressionF[B] = exp match {
      case IntValueF(v)    => IntValueF(v)
      case DecValueF(v)    => DecValueF(v)
      case SumF(l, r)      => SumF(f(l), f(r))
      case MultiplyF(l, r) => MultiplyF(f(l), f(r))
      case DivideF(l, r)   => DivideF(f(l), f(r))
      case SquareF(v)      => SquareF(f(v))
    }
  }

/** F-Algebra is a function that actually evaluates the values. `Algebra[Exp, Double]` is equivalent to `Exp[Double] => Double`. Think of this as "what to do" at each destination the functor gets us
  * to.
  */
lazy val expressionAlgebra: Algebra[ExpressionF, Double] =
  Algebra {
    case IntValueF(v)    => v.toDouble
    case DecValueF(v)    => v
    case SumF(l, r)      => l + r
    case MultiplyF(l, r) => l * r
    case DivideF(l, r)   => l / r
    case SquareF(v)      => v * v
  }
