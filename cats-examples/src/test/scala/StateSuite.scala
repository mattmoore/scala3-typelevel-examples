import cats.syntax.all.*
import cats.data.*
import weaver.*
import cats.Eval

object StateSuite extends SimpleIOSuite {
  pureTest("Updating a state containing a list of values found matching a criteria") {
    case class Categories(
        even: List[Int],
        odd: List[Int],
        div5: List[Int],
    )

    object MyState {
      lazy val empty: Categories = Categories(Nil, Nil, Nil)
    }

    def collectEvenOrOdd(next: Int): State[Categories, Unit] = State.modify { (s: Categories) =>
      if (next % 2 == 0) s.copy(even = next :: s.even)
      else s.copy(odd = next :: s.odd)
    }

    def collectDivBy5(next: Int): State[Categories, Unit] = State.modify { s =>
      if (next % 5 == 0) s.copy(div5 = next :: s.div5)
      else s
    }

    val result = (1 to 10).toList.foldLeft(MyState.empty) { (s, next) =>
      (
        collectEvenOrOdd(next)
          *> collectDivBy5(next)
      ).runS(s).value
    }

    expect(
      result == Categories(
        even = List(10, 8, 6, 4, 2),
        odd = List(9, 7, 5, 3, 1),
        div5 = List(10, 5),
      ),
    )
  }

  pureTest("Find matching words - with state monad") {
    val phrase     = "listofwords"
    val dictionary = List("list", "of", "words")

    case class Tracking(wordsFound: List[String] = List.empty, prevWord: String = "")

    def processNextChar(nextChar: Char): State[Tracking, Unit] = State.modify { s =>
      val nextWord = s.prevWord + nextChar
      dictionary.contains(nextWord) match {
        case true  => Tracking(s.wordsFound :+ nextWord, "")
        case false => Tracking(s.wordsFound, nextWord)
      }
    }

    val matchingWords = phrase.foldLeft(new Tracking) { (s, c) =>
      processNextChar(c).runS(s).value
    }

    expect(
      matchingWords.wordsFound == List("list", "of", "words"),
    )
  }

  pureTest("Find matching words = without state monad - arguably this is simpler for more folks to understand") {
    val phrase     = "listofwords"
    val dictionary = List("list", "of", "words")

    case class Tracking(wordsFound: List[String] = List.empty, prevWord: String = "")

    val matchingWords: Tracking = phrase.foldLeft(new Tracking) { (s, nextChar) =>
      val nextWord = s.prevWord + nextChar
      dictionary.contains(nextWord) match {
        case true  => Tracking(s.wordsFound :+ nextWord, "")
        case false => Tracking(s.wordsFound, nextWord)
      }
    }

    expect(
      matchingWords.wordsFound == List("list", "of", "words"),
    )
  }

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
}
