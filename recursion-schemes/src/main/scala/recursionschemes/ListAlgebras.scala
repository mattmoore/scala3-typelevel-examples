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

  // sealed trait ListF[+A, +B]
  // final case class ConsF[A, B](head: A, tail: B) extends ListF[A, B]
  // case object NilF                               extends ListF[Nothing, Nothing]

  // given Functor[ListF[Int, *]] = new Functor[ListF[Int, *]] {
  //   def map[A, B](fa: ListF[A, ?])(f: A => B): ListF[B, ?] = fa match {
  //     case ConsF[Int, A](head, tail) => ConsF(head, f(tail))
  //     case NilF              => NilF
  //   }
  // }

  // val doubleAlgebra: Algebra[ListF[Int, *], Int] = Algebra {
  //   case ConsF(head, tail) => head * tail
  //   case NilF              => 1
  // }
}
