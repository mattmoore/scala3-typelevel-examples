import higherkindness.droste.*
import higherkindness.droste.data.*
import weaver.*

object DrosteSuite extends SimpleIOSuite {
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

    expect.all(
      fib(0) == BigDecimal(0),
      fib(1) == BigDecimal(1),
      fib(2) == BigDecimal(1),
      fib(10) == BigDecimal(55),
      fib(100) == BigDecimal("354224848179261915075"),
    )
  }
}
