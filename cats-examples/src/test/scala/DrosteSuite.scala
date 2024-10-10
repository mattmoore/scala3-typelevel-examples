import higherkindness.droste.*
import higherkindness.droste.data.*
import weaver.*

object DrosteSuite extends SimpleIOSuite {
  val natCoalgebra: Coalgebra[Option, BigDecimal] =
    Coalgebra(n => if (n > 0) Some(n - 1) else None)

  val fibAlgebra: CVAlgebra[Option, BigDecimal] = CVAlgebra {
    case Some(r1 :< Some(r2 :< _)) => r1 + r2
    case Some(_ :< None)           => 1
    case None                      => 0
  }

  pureTest("Fibonacci using an anamorphism followed by a histomorphism - aka a dynamorphism") {
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
}
