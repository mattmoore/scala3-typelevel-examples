package recursionschemes.algebras.simpleexpression

sealed trait ExpressionF[+A]
final case class IntValueF[A](v: Int)     extends ExpressionF[A]
final case class DecValueF[A](v: Double)  extends ExpressionF[A]
final case class SumF[A](x: A, y: A)      extends ExpressionF[A]
final case class MultiplyF[A](x: A, y: A) extends ExpressionF[A]
final case class DivideF[A](x: A, y: A)   extends ExpressionF[A]
final case class SquareF[A](x: A)         extends ExpressionF[A]
