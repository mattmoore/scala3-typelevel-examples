class CompanionObjects extends munit.FunSuite {
  test("Companion objects are like java's static methods") {
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
    assertEquals(actual1, "Hello, Matt")

    val actual2 = Person.greet(p2)
    println(s"RESULT 2: $actual2")
    assertEquals(actual2, "Hello, Kairo")

    val actual3 = p2.greet
    println(s"RESULT 2: $actual3")
    assertEquals(actual3, "Hello, Kairo")
  }
}
