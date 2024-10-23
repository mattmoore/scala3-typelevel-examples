package recursionschemes.algebras.list

import cats.Functor
import higherkindness.droste.*
import higherkindness.droste.data.*
import higherkindness.droste.syntax.all.*
import recursionschemes.algebras.*

sealed trait ListF[+A]
final case class ConsF[A](head: Int, tail: A) extends ListF[A]
case object NilF                              extends ListF[Nothing]

lazy val iso: IsoSet[List[Int], Fix[ListF]] =
  new (List[Int] <=> Fix[ListF]) {
    val forward: List[Int] => Fix[ListF] = {
      case Nil          => NilF.fix
      case head :: tail => ConsF(head, forward(tail)).fix
    }

    val inverse: Fix[ListF] => List[Int] = {
      case NilF                   => Nil
      case Fix(ConsF(head, tail)) => head :: inverse(tail)
    }
  }

implicit val listFFunctor: Functor[ListF] = new Functor[ListF] {
  def map[A, B](fa: ListF[A])(f: A => B): ListF[B] = fa match {
    case ConsF(head, tailResult) => ConsF(head, f(tailResult))
    case NilF                    => NilF
  }
}

lazy val sumAlgebra: Algebra[ListF, Int] = Algebra {
  case NilF              => 0
  case ConsF(head, tail) => head + tail
}

lazy val productAlgebra: Algebra[ListF, Int] = Algebra {
  case ConsF(head, tail) => head * tail
  case NilF              => 1
}

lazy val doubleAlgebra: Algebra[ListF, List[Int]] = Algebra {
  case NilF              => List.empty
  case ConsF(head, tail) => head * 2 :: tail
}

lazy val stringAlgebra: Algebra[ListF, String] = Algebra {
  case NilF              => "Nil"
  case ConsF(head, tail) => s"$head :: $tail"
}
