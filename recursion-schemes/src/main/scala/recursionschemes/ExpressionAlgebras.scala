package recursionschemes

import cats.*
import higherkindness.droste.*
import higherkindness.droste.syntax.all.*

object ExpressionAlgebras {
  sealed trait Expr[A]
  final case class IntValue[A](v: Int)           extends Expr[A]
  final case class DecValue[A](v: Double)        extends Expr[A]
  final case class Sum[A](exp1: A, exp2: A)      extends Expr[A]
  final case class Multiply[A](exp1: A, exp2: A) extends Expr[A]
  final case class Divide[A](exp1: A, exp2: A)   extends Expr[A]
  final case class Square[A](exp: A)             extends Expr[A]

  // Functor - this defines how to map over expression types, recursively
  given Functor[Expr] = new Functor[Expr] {
    def map[A, B](exp: Expr[A])(f: A => B): Expr[B] = exp match {
      case IntValue(v)      => IntValue(v)
      case DecValue(v)      => DecValue(v)
      case Sum(a1, a2)      => Sum(f(a1), f(a2))
      case Multiply(a1, a2) => Multiply(f(a1), f(a2))
      case Divide(a1, a2)   => Divide(f(a1), f(a2))
      case Square(a)        => Square(f(a))
    }
  }

  // F-Algebra - function that actually evaluates the values
  // Algebra[Exp, Double] is isomorphic to Exp[Double] => Double
  val evaluateAlgebra: Algebra[Expr, Double] = Algebra {
    case IntValue(v)      => v.toDouble
    case DecValue(v)      => v
    case Sum(a1, a2)      => a1 + a2
    case Multiply(a1, a2) => a1 * a2
    case Divide(a1, a2)   => a1 / a2
    case Square(a)        => a * a
  }
}
