import cats.Functor
import higherkindness.droste.*
import higherkindness.droste.data.*
import higherkindness.droste.syntax.all.*
import weaver.*

object DrosteSuite extends SimpleIOSuite {
  pureTest("Catamorphism with expression example - all you need is a Functor and F-Algebra") {
    sealed trait Exp[A]
    final case class IntValue[A](v: Int)           extends Exp[A]
    final case class DecValue[A](v: Double)        extends Exp[A]
    final case class Sum[A](exp1: A, exp2: A)      extends Exp[A]
    final case class Multiply[A](exp1: A, exp2: A) extends Exp[A]
    final case class Divide[A](exp1: A, exp2: A)   extends Exp[A]
    final case class Square[A](exp: A)             extends Exp[A]

    // Functor - this defines how to map over expression types
    given functor: Functor[Exp] = new Functor[Exp] {
      def map[A, B](exp: Exp[A])(f: A => B): Exp[B] = exp match {
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
    val evaluate: Algebra[Exp, Double] = Algebra {
      case IntValue(v)      => v.toDouble
      case DecValue(v)      => v
      case Sum(a1, a2)      => a1 + a2
      case Multiply(a1, a2) => a1 * a2
      case Divide(a1, a2)   => a1 / a2
      case Square(a)        => a * a
    }

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

  pureTest("Fibonacci using an anamorphism followed by a histomorphism - aka a dynamorphism") {
    val natCoalgebra: Coalgebra[Option, BigDecimal] =
      Coalgebra(n => if (n > 0) Some(n - 1) else None)

    val fibAlgebra: CVAlgebra[Option, BigDecimal] = CVAlgebra {
      case Some(r1 :< Some(r2 :< _)) => r1 + r2
      case Some(_ :< None)           => 1
      case None                      => 0
    }

    val fib: BigDecimal => BigDecimal = scheme.ghylo(
      fibAlgebra.gather(Gather.histo),
      natCoalgebra.scatter(Scatter.ana),
    )

    // As mentioned, an anamorphism followed by a histomorphism is a dynamorphism.
    // droste provides a recursion scheme for this:
    val fibAlt: BigDecimal => BigDecimal =
      scheme.zoo.dyna(fibAlgebra, natCoalgebra)

    expect.all(
      fib(0) == BigDecimal(0),
      fib(1) == BigDecimal(1),
      fib(2) == BigDecimal(1),
      fib(10) == BigDecimal(55),
      fib(100) == BigDecimal("354224848179261915075"),
      fibAlt(0) == BigDecimal(0),
      fibAlt(1) == BigDecimal(1),
      fibAlt(2) == BigDecimal(1),
      fibAlt(10) == BigDecimal(55),
      fibAlt(100) == BigDecimal("354224848179261915075"),
    )
  }

  pureTest("Do two things at once - fibonacci and sum of all squares") {
    val natCoalgebra: Coalgebra[Option, BigDecimal] =
      Coalgebra(n => if (n > 0) Some(n - 1) else None)

    val fibAlgebra: CVAlgebra[Option, BigDecimal] = CVAlgebra {
      case Some(r1 :< Some(r2 :< _)) => r1 + r2
      case Some(_ :< None)           => 1
      case None                      => 0
    }

    val fromNatAlgebra: Algebra[Option, BigDecimal] = Algebra {
      case Some(n) => n + 1
      case None    => 0
    }

    // note: n is the fromNatAlgebra helper value from the previous level of recursion
    val sumSquaresAlgebra: RAlgebra[BigDecimal, Option, BigDecimal] = RAlgebra {
      case Some((n, value)) => value + (n + 1) * (n + 1)
      case None             => 0
    }

    val sumSquares: BigDecimal => BigDecimal = scheme.ghylo(
      sumSquaresAlgebra.gather(Gather.zygo(fromNatAlgebra)),
      natCoalgebra.scatter(Scatter.ana),
    )

    val fused: BigDecimal => (BigDecimal, BigDecimal) =
      scheme.ghylo(
        fibAlgebra.gather(Gather.histo) zip sumSquaresAlgebra.gather(Gather.zygo(fromNatAlgebra)),
        natCoalgebra.scatter(Scatter.ana),
      )

    expect.all(
      sumSquares(0) == 0,
      sumSquares(1) == 1,
      sumSquares(2) == 5,
      sumSquares(10) == 385,
      sumSquares(100) == 338350,
      fused(0) == (0, 0),
      fused(1) == (1, 1),
      fused(2) == (1, 5),
      fused(10) == (55, 385),
      fused(100) == (BigDecimal("354224848179261915075"), 338350),
    )
  }

  pureTest("Do many things at once") {
    sealed trait ListIntF[+T]

    final case class ::[+T](head: Int, tail: T) extends ListIntF[T]

    case object Nil extends ListIntF[Nothing]

    implicit class ConsInt[T](t: T) {
      def ::(newHead: Int): ::[T] = new ::(newHead, t)
    }

    val sumAlgebra: Algebra[ListIntF, Int] = Algebra {
      case head :: tailResult => head + tailResult
      case Nil                => 0
    }

    val sizeAlgebra: Algebra[ListIntF, Int] = Algebra {
      case _ :: tailResult => 1 + tailResult
      case Nil             => 0
    }

    val mkStringAlgebra: Algebra[ListIntF, String] = Algebra {
      case value :: other => value.toString + " :: " + other
      case Nil            => "Nil"
    }

    val nListCoalgebra: Coalgebra[ListIntF, Int] = Coalgebra {
      case n if n > 0 => n :: (n - 1)
      case _          => Nil
    }

    implicit val listIntFunctor: Functor[ListIntF] = new Functor[ListIntF] {
      override def map[A, B](fa: ListIntF[A])(f: A => B): ListIntF[B] = fa match {
        case head :: tail => head :: f(tail)
        case Nil          => Nil
      }
    }

    val doManyThings = scheme.ghylo(
      (sumAlgebra `zip` sizeAlgebra `zip` mkStringAlgebra).gather(Gather.cata),
      nListCoalgebra.scatter(Scatter.ana),
    )

    expect.all(
      doManyThings(4) == ((10, 4), "4 :: 3 :: 2 :: 1 :: Nil"),
    )
  }

  pureTest("Simple example of recursion over List Int and fold to either Int or String without droste") {
    sealed trait List

    case class Cons(
        head: Int,
        tail: List,
    ) extends List

    case object Nil extends List

    def product(
        values: List,
    ): Int =
      values match {
        case Cons(head, tail) => head * product(tail)
        case Nil              => 1
      }

    def mkString(
        values: List,
    ): String =
      values match {
        case Cons(head, tail) => s"$head :: ${mkString(tail)}"
        case Nil              => "Nil"
      }

    val ints: List = Cons(3, Cons(2, Cons(1, Nil)))

    expect.all(
      product(ints) == 3 * 2 * 1,
      mkString(ints) == "3 :: 2 :: 1 :: Nil",
    )
  }

  pureTest("Simple example of recursion over List String without droste") {
    sealed trait List

    case class Cons(
        head: Int,
        tail: List,
    ) extends List

    case object Nil extends List

    def stepString(head: Int, tailResult: String): String =
      s"$head :: $tailResult"

    def stepInt(head: Int, tailResult: Int): Int =
      head * tailResult

    def fold[A](
        base: A,
        step: (Int, A) => A,
    ): List => A = {
      def loop(state: List): A =
        state match {
          case Cons(head, tail) => step(head, loop(tail))
          case Nil              => base
        }

      loop
    }

    def mkString: List => String =
      fold("Nil", stepString)

    def product: List => Int =
      fold(1, stepInt)

    val ints: List = Cons(3, Cons(2, Cons(1, Nil)))

    expect.all(
      fold("Nil", stepString)(ints) == "3 :: 2 :: 1 :: Nil",
      fold(1, stepInt)(ints) == 6,
      mkString(ints) == "3 :: 2 :: 1 :: Nil",
      product(ints) == 6,
    )
  }

  pureTest("Abstract the structure") {
    sealed trait List

    case class Cons(
        head: Int,
        tail: List,
    ) extends List

    case object Nil extends List

    val project: List => Option[(Int, List)] = {
      case Cons(head, tail) => Some((head, tail))
      case Nil              => None
    }

    val opString: Option[(Int, String)] => String = {
      case Some((head, tailResult)) => s"$head :: $tailResult"
      case None                     => "nil"
    }

    val opInt: Option[(Int, Int)] => Int = {
      case Some((head, tailResult)) => head * tailResult
      case None                     => 1
    }

    def fold[A](
        op: Option[(Int, A)] => A,
        projection: List => Option[(Int, List)],
    ): List => A = {
      def loop(state: List): A =
        op(projection(state) match {
          case Some((head, tail)) => Some(head, loop(tail))
          case None               => None
        })

      loop
    }

    def mkString: List => String =
      fold(opString, project)

    def product: List => Int =
      fold(opInt, project)

    val ints: List = Cons(3, Cons(2, Cons(1, Nil)))

    expect.all(
      fold(opString, project)(ints) == "3 :: 2 :: 1 :: nil",
      fold(opInt, project)(ints) == 6,
      mkString(ints) == "3 :: 2 :: 1 :: nil",
      product(ints) == 6,
    )
  }

  pureTest("Abstract the structure 2") {
    sealed trait List

    case class Cons(
        head: Int,
        tail: List,
    ) extends List

    case object Nil extends List

    type ListF[A] = Option[(Int, A)]

    val project: List => ListF[List] = {
      case Cons(head, tail) => Some((head, tail))
      case Nil              => None
    }

    val opString: ListF[String] => String = {
      case Some((head, tailResult)) => s"$head :: $tailResult"
      case None                     => "nil"
    }

    val opInt: ListF[Int] => Int = {
      case Some((head, tailResult)) => head * tailResult
      case None                     => 1
    }

    def fold[A](
        op: ListF[A] => A,
        projection: List => ListF[List],
    ): List => A = {
      def loop(state: List): A =
        op(go(projection(state), loop))

      def go(state: ListF[List], f: List => A): ListF[A] =
        state match {
          case Some((head, tail)) => Some(head, f(tail))
          case None               => None
        }

      loop
    }

    def mkString: List => String =
      fold(opString, project)

    def product: List => Int =
      fold(opInt, project)

    val ints: List = Cons(3, Cons(2, Cons(1, Nil)))

    expect.all(
      fold(opString, project)(ints) == "3 :: 2 :: 1 :: nil",
      fold(opInt, project)(ints) == 6,
      mkString(ints) == "3 :: 2 :: 1 :: nil",
      product(ints) == 6,
    )
  }

  pureTest("Abstract the structure 3 - introduce functor") {
    type ListF[A] = Option[(Int, A)]

    trait Functor[F[_]] {
      def map[A, B](fa: F[A], f: A => B): F[B]
    }

    implicit val listFFunctor = new Functor[ListF] {
      override def map[A, B](list: ListF[A], f: A => B) =
        list match {
          case Some((head, tail)) => Some((head, f(tail)))
          case None               => None
        }
    }

    def map[F[_], A, B](
        fa: F[A],
        f: A => B,
    )(implicit
        functor: Functor[F],
    ): F[B] =
      functor.map(fa, f)

    sealed trait List

    case class Cons(
        head: Int,
        tail: List,
    ) extends List

    case object Nil extends List

    val project: List => ListF[List] = {
      case Cons(head, tail) => Some((head, tail))
      case Nil              => None
    }

    val opString: ListF[String] => String = {
      case Some((head, tailResult)) => s"$head :: $tailResult"
      case None                     => "nil"
    }

    val opInt: ListF[Int] => Int = {
      case Some((head, tailResult)) => head * tailResult
      case None                     => 1
    }

    def cata[F[_]: Functor, A, B](
        algebra: F[A] => A,
        projection: B => F[B],
    ): B => A = {
      def loop(state: B): A =
        algebra(map(projection(state), loop))

      loop
    }

    def mkString: List => String =
      cata(opString, project)

    def product: List => Int =
      cata(opInt, project)

    val ints: List = Cons(3, Cons(2, Cons(1, Nil)))

    expect.all(
      cata(opString, project).apply(ints) == "3 :: 2 :: 1 :: nil",
      cata(opInt, project).apply(ints) == 6,
      mkString(ints) == "3 :: 2 :: 1 :: nil",
      product(ints) == 6,
    )
  }
}
