package recursionschemes.algebras.simpleexpression

sealed trait Expression
final case class IntValue(v: Int)        extends Expression
final case class DecValue(v: Double)     extends Expression
final case class Sum[A](x: A, y: A)      extends Expression
final case class Multiply[A](x: A, y: A) extends Expression
final case class Divide[A](x: A, y: A)   extends Expression
final case class Square[A](x: A)         extends Expression
