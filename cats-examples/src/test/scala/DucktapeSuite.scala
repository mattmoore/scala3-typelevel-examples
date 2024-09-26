import weaver.*

import io.github.arainko.ducktape.*

object DucktapeSuite extends SimpleIOSuite {
  pureTest("Transforms from one class to another") {
    object domain {
      final case class Person(
          firstName: String,
          lastName: String,
      )
    }

    object wire {
      final case class Person(
          firstName: String,
          lastName: String,
      )
    }

    val wirePerson = wire.Person("Matt", "Moore")

    expect(
      wirePerson.to[domain.Person] == domain.Person("Matt", "Moore"),
    )
  }
}
