import weaver.*

// To demonstrate how we can implement semigroup ourselves, without relying on cats.
// Note that we have no imports in this file from cats.
// I'm only importing my own custom-defined instances and syntax.

object ScratchSemigroupSuite extends SimpleIOSuite {
  // We define the case classes that will hold user session data.
  case class Session(sessionId: Int, creationTime: Long)
  case class SessionState(identityId: Int, sessions: List[Session])

  trait Semigroup[T] {
    def combine(x: T, y: T): T
  }

  object Semigroup {
    def apply[T](using instance: Semigroup[T]): Semigroup[T] = instance
  }

  object SemigroupInstances {
    // Next we define a semigroup instance for SessionState
    // Note we're overriding the combine function, implementing the logic for combining SessionState.
    // The logic here basically is:
    //   if both SessionState have the same identityId, return a new SessionState with the sessions from both
    //   else if both SessionState have different identityId, return the second one
    given Semigroup[SessionState] with {
      override def combine(x: SessionState, y: SessionState): SessionState =
        if (x.identityId == y.identityId) {
          SessionState(
            identityId = y.identityId,
            sessions = x.sessions ++ y.sessions,
          )
        } else y
    }
  }

  val sessionState1 = SessionState(
    identityId = 1,
    sessions = List(Session(1, 1000L)),
  )

  val sessionState2 = SessionState(
    identityId = 1,
    List(Session(2, 2000L)),
  )

  val expected = SessionState(
    identityId = 1,
    List(
      Session(1, 1000L),
      Session(2, 2000L),
    ),
  )

  pureTest("Let's define our own Semigroup instead of relying on cats") {
    // Now we can import our instances...
    import SemigroupInstances.given
    // ...and combine:
    val actual = Semigroup[SessionState].combine(sessionState1, sessionState2)
    expect(actual == expected)
  }

  pureTest("Let's define extension methods just like cats does") {
    object SemigroupSyntax {
      extension [T](a: T)
        infix def combine(b: T)(using semigroup: Semigroup[T]): T =
          semigroup.combine(a, b)

        def |+|(b: T)(using semigroup: Semigroup[T]): T =
          a.combine(b)
    }

    // Now we can import our instances...
    import SemigroupInstances.given
    // ...and import the syntax containing our extension methods...
    import SemigroupSyntax.*

    // ...and now use them:

    // Calling it "normally" with dot notation and parens:
    val actual1 = sessionState1.combine(sessionState2)
    // With infix notation:
    val actual2 = sessionState1 combine sessionState2
    // With the infix symbolic syntax:
    val actual3 = sessionState1 |+| sessionState2

    expect.all(
      actual1 == expected,
      actual2 == expected,
      actual3 == expected,
    )
  }
}
