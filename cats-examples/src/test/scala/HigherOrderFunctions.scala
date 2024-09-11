import weaver.*

// The purpose of the scenarios below is to demonstrate how we can write higher-order functions in Scala.
// I compare different approaches below, showing a much easier approach with by-name parameters.
object HigherOrderFunctions extends SimpleIOSuite {
  pureTest("Regular higher order function") {
    // With a regular higher-order function, we have to pass the function by name, and the parameter to call it with.
    def timer[A](f: (Double) => A, x: Double): (A, Double) = {
      val startTime = System.nanoTime
      val result    = f(x)
      val stopTime  = System.nanoTime
      val delta     = stopTime - startTime
      (result, delta / 1000000d)
    }

    def calculate(input: Double) = input * 37

    val (result, duration) = timer(calculate, 1)

    expect.all(
      result == 37,
      duration > 0,
    )
  }

  pureTest("Function value (lambda, aka anonymous function)") {
    // In Scala 2, we don't have the ability to pass type parameters to a lambda.
    // This means two things:
    //   1. We can just define timer as the function type, without passing the x to it. (pro)
    //   2. We have to define multiple lambdas for different input/output types. (con)
    val timerDouble: (Double) => (Double, Double) = { f =>
      val startTime = System.nanoTime
      val result    = f
      val stopTime  = System.nanoTime
      val delta     = stopTime - startTime
      (result, delta / 1000000d)
    }
    val timerInt: (Double) => (Double, Double) = { f =>
      val startTime = System.nanoTime
      val result    = f
      val stopTime  = System.nanoTime
      val delta     = stopTime - startTime
      (result, delta / 1000000d)
    }

    def calculateDouble(input: Double): Double = input * 37
    def calculateInt(input: Int): Int          = input * 37

    val (doubleResult, doubleDuration) = timerDouble(calculateDouble(1))
    val (intResult, intDuration)       = timerInt(calculateInt(1))

    expect.all(
      doubleResult == 37,
      doubleDuration > 0,
      intResult == 37,
      intDuration > 0,
    )
  }

  pureTest("Scala 3 polymorphic functions") {
    // In Scala 3, we can define a polymorphic function value - in other words, Scala 3 lambdas can take type parameters.
    val timer: [A] => A => (A, Double) = [A] => { f =>
      val startTime = System.nanoTime
      val result    = f
      val stopTime  = System.nanoTime
      val delta     = stopTime - startTime
      (result, delta / 1000000d)
    }

    def calculateDouble(input: Double): Double = input * 37
    def calculateInt(input: Int): Int          = input * 37

    val (doubleResult, doubleDuration) = timer(calculateDouble(1))
    val (intResult, intDuration)       = timer(calculateInt(1))

    expect.all(
      doubleResult == 37,
      doubleDuration > 0,
      intResult == 37,
      intDuration > 0,
    )
  }

  pureTest("Finally, we can do this easier with by-name parameters") {
    // We make a parameter "by-name" by prepending it's type with `=>` - for example, below we have `f: => A`.
    // This means f() will only be evaluated when accessed inside the timer function.
    // This also means the execution time will be accurate, because the evaluation is sandwiched between startTime/endTime.
    // If we had done this without by-name parameters, we wouldn't get an accurate duration.
    // This is because f would be evaluated eagerly, rather than in between startTime/endTime.
    // Additionally, we can define this as a function with a generic return type.
    def timer[A](f: => A): (A, Double) = {
      val startTime = System.nanoTime
      val result    = f
      val stopTime  = System.nanoTime
      val delta     = stopTime - startTime
      (result, delta / 1000000d)
    }

    def calculateDouble(input: Double): Double = input * 37
    def calculateInt(input: Int): Int          = input * 37

    val (doubleResult, doubleDuration) = timer(calculateDouble(1))
    val (intResult, intDuration)       = timer(calculateInt(1))

    expect.all(
      doubleResult == 37,
      doubleDuration > 0,
      intResult == 37,
      intDuration > 0,
    )
  }
}
