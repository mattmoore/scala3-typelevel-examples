import fs2.Stream
import weaver.*

object IntroStreamSuite extends SimpleIOSuite {

  // Stream construction:

  pureTest("Streams can be empty") {
    expect(Stream.empty.toList == List.empty)
  }

  pureTest("Stream.emit can initialize a stream") {
    expect(Stream.emit(1).toList == List(1))
  }

  pureTest("A stream can be initialized with varargs in the constructor") {
    expect(Stream(1, 2, 3).toList == List(1, 2, 3))
  }

  pureTest("Stream.emits can initialize with a List") {
    expect(Stream.emits(List(1, 2, 3)).toList == List(1, 2, 3))
  }

  pureTest("Streams have list-like functionality: ++") {
    expect(
      (Stream(1, 2, 3) ++ Stream(4, 5)).toList == List(1, 2, 3, 4, 5),
    )
  }

  // List-like functionality:

  pureTest("Streams have list-like functionality: map") {
    expect(
      Stream(1, 2, 3).map(_ + 1).toList == List(2, 3, 4),
    )
  }

  pureTest("Streams have list-like functionality: filter") {
    expect(
      Stream(1, 2, 3).filter(_ % 2 != 0).toList == List(1, 3),
    )
  }

  pureTest("Streams have list-like functionality: fold") {
    expect(
      Stream(1, 2, 3).fold(0)(_ + _).toList == List(6),
    )
  }

  pureTest("Streams have list-like functionality: collect") {
    expect(
      Stream(None, Some(2), Some(3)).collect { case Some(i) => i }.toList == List(2, 3),
    )
  }

  pureTest("Streams have list-like functionality: intersperser") {
    expect(
      Stream.range(0, 5).intersperse(42).toList == List(0, 42, 1, 42, 2, 42, 3, 42, 4),
    )
  }

  pureTest("Streams have list-like functionality: flatMap") {
    expect(
      Stream(1, 2, 3).flatMap(i => Stream(i, i)).toList == List(1, 1, 2, 2, 3, 3),
    )
  }

  pureTest("Streams have list-like functionality: repeat.take") {
    expect(
      Stream(1, 2, 3).repeat.take(9).toList == List(1, 2, 3, 1, 2, 3, 1, 2, 3),
    )
  }

  pureTest("Streams have list-like functionality: repeatN") {
    expect(
      Stream(1, 2, 3).repeatN(2).toList == List(1, 2, 3, 1, 2, 3),
    )
  }
}
