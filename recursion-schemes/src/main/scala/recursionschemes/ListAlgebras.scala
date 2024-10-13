package recursionschemes

import cats.Functor
import higherkindness.droste.*

object ListAlgebras {
  sealed trait ListF[+A]
  final case class ConsF[A](head: Int, tail: A) extends ListF[A]
  case object NilF                              extends ListF[Nothing]

  given Functor[ListF] = new Functor[ListF] {
    def map[A, B](fa: ListF[A])(f: A => B): ListF[B] = fa match {
      case ConsF(head, tailResult) => ConsF(head, f(tailResult))
      case NilF                    => NilF
    }
  }

  val doubleAlgebra: Algebra[ListF, Int] = Algebra {
    case ConsF(head, tail) => head * tail
    case NilF              => 1
  }
}
