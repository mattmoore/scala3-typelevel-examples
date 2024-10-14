package recursionschemes

import cats.Functor
import higherkindness.droste.*
import higherkindness.droste.data.*
import higherkindness.droste.syntax.all.*

object ListAlgebras {
  sealed trait ListF[+A]
  final case class ConsF[A](head: Int, tail: A) extends ListF[A]
  case object NilF                              extends ListF[Nothing]

  def in: ListF[List[Int]] => List[Int] = {
    case NilF              => Nil
    case ConsF(head, tail) => head :: tail
  }

  def out: List[Int] => Fix[ListF] = {
    case Nil          => NilF.fix
    case head :: tail => ConsF(head, out(tail)).fix
  }

  given Functor[ListF] = new Functor[ListF] {
    def map[A, B](fa: ListF[A])(f: A => B): ListF[B] = fa match {
      case ConsF(head, tailResult) => ConsF(head, f(tailResult))
      case NilF                    => NilF
    }
  }

  val productAlgebra: Algebra[ListF, Int] = Algebra {
    case ConsF(head, tail) => head * tail
    case NilF              => 1
  }
}
