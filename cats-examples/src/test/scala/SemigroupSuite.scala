import cats.kernel.Semigroup

class SemigroupSuite extends munit.FunSuite {
  // We define the case classes that will hold user session data.
  case class Session(sessionId: Int, creationTime: Long)
  case class SessionState(identityId: Int, sessions: List[Session])

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
          sessions = x.sessions ++ y.sessions
        )
      } else y
  }

  val sessionState1 = SessionState(
    identityId = 1,
    sessions = List(Session(1, 1000L))
  )

  val sessionState2 = SessionState(
    identityId = 1,
    List(Session(2, 2000L))
  )

  val expected = SessionState(
    identityId = 1,
    List(
      Session(1, 1000L),
      Session(2, 2000L)
    )
  )

  test("Semigroup allows us to combine types together") {
    val actual = Semigroup[SessionState].combine(sessionState1, sessionState2)
    assertEquals(actual, expected)
  }

  test("Syntax helpers") {
    // There's also a convenient method we can use |+|
    // This requires an import:
    import cats.implicits.catsSyntaxSemigroup

    val actual = sessionState1 combine sessionState2
    assertEquals(actual, expected)
  }

  test("Symbolic syntax") {
    // For the symbolic syntax, we still need to import the implicits:
    import cats.implicits.catsSyntaxSemigroup

    // And now we can use |+|
    val actual = sessionState1 |+| sessionState2
    assertEquals(actual, expected)
  }
}
