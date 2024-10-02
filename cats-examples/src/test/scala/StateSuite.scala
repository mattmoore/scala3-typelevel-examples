import cats.data.State
import weaver.*
import cats.data.IndexedStateT
import cats.Eval

object StateSuite extends SimpleIOSuite {
  pureTest("Robot example") {
    final case class Robot(id: Long, sentient: Boolean, name: String, model: String)

    final case class Seed(long: Long) {
      def next = Seed(long * 6364136223846793005L + 1442695040888963407L)
    }

    val nextLong: State[Seed, Long]       = State(seed => (seed.next, seed.long))
    val nextBoolean: State[Seed, Boolean] = nextLong.map(long => long >= 0)

    val createRobot: State[Seed, Robot] =
      for {
        id          <- nextLong
        sentient    <- nextBoolean
        isCatherine <- nextBoolean
        name = if (isCatherine) "Catherine" else "Carlos"
        isReplicant <- nextBoolean
        model = if (isReplicant) "replicant" else "borg"
      } yield Robot(id, sentient, name, model)

    val initialSeed = Seed(13L)

    val (finalState, robot) = createRobot.run(initialSeed).value

    expect.all(
      finalState == Seed(2999987205171331217L),
      robot == Robot(
        id = 13L,
        sentient = false,
        name = "Catherine",
        model = "replicant",
      ),
    )
  }

  pureTest("Door state example") {
    sealed trait DoorState
    case object Open   extends DoorState
    case object Closed extends DoorState

    def open: IndexedStateT[Eval, Closed.type, Open.type, Unit]  = IndexedStateT.set(Open)
    def close: IndexedStateT[Eval, Open.type, Closed.type, Unit] = IndexedStateT.set(Closed)

    val valid = for {
      _ <- open
      _ <- close
      _ <- open
    } yield ()

    val result = valid.runS(Closed).value

    expect.all(
      result == Open,
    )
  }

  pureTest("") {
    val stateMonad: State[Int, Int] = State(s => (1, 1))
    val result                      = stateMonad.runA(1).value

    expect(
      result == 1,
    )
  }
}
