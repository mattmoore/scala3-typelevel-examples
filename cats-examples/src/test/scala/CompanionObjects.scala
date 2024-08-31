import weaver.*

object CompanionObjects extends SimpleIOSuite {
  pureTest("Companion objects are like java's static methods") {
    case class Person(name: String) {
      def greet: String =
        s"Hello, ${name}"
    }

    object Person {
      def greet(p: Person): String =
        s"Hello, ${p.name}"
    }

    val p1 = Person("Matt")
    val p2 = Person("Kairo")

    val actual1 = Person.greet(p1)
    println(s"RESULT 1: $actual1")

    val actual2 = Person.greet(p2)
    println(s"RESULT 2: $actual2")

    val actual3 = p2.greet
    println(s"RESULT 2: $actual3")

    expect.all(
      actual1 == "Hello, Matt",
      actual2 == "Hello, Kairo",
      actual3 == "Hello, Kairo",
    )
  }
}
