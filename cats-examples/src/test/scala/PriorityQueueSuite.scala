import cats.Order
import cats.effect.IO
import cats.effect.std.PQueue
import cats.implicits.*
import weaver.*

object PriorityQueueSuite extends SimpleIOSuite {
  test("PriorityQueue with ints") {
    val list = List(1, 4, 3, 7, 5, 2, 6, 9, 8)

    implicit val orderForInt: Order[Int] = Order.fromLessThan((x, y) => x < y)

    def processQueue(list: List[Int]) = for {
      pq <- PQueue.bounded[IO, Int](10)
      _  <- list.traverse(pq.offer(_))
      l  <- List.fill(list.length)(()).traverse(_ => pq.take)
    } yield l

    for {
      result <- processQueue(list)
      _      <- IO.println(result)
    } yield expect.all(
      result == List(1, 2, 3, 4, 5, 6, 7, 8, 9),
    )
  }

  test("PriorityQueue with case class containing a priority") {
    case class Person(name: String)
    case class Message(priority: Int, person: Person)

    val messages = List(
      Message(1, Person("Matt")),
      Message(2, Person("John")),
    )

    implicit val orderForMessage: Order[Message] =
      Order.fromLessThan((x, y) => x.priority < y.priority)

    def processQueue(list: List[Message]) = for {
      pq <- PQueue.bounded[IO, Message](10)
      _  <- messages.traverse(pq.offer(_))
      l  <- List.fill(messages.length)(()).traverse(_ => pq.take)
    } yield l

    for {
      result <- processQueue(messages)
      _      <- IO.println(result)
    } yield expect.all(
      result == List(
        Message(1, Person("Matt")),
        Message(2, Person("John")),
      ),
    )
  }
}
