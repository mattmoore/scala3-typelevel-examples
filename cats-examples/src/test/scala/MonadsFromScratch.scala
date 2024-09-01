import weaver.*

import scala.annotation.nowarn

object MonadsFromScratch extends SimpleIOSuite {
  trait Functor[F[_]] {
    def map[A, B](fa: F[A])(f: A => B): F[B]
  }

  trait Apply[F[_]] extends Functor[F] {
    def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
  }

  trait Applicative[F[_]] extends Apply[F] {
    def pure[A](a: A): F[A]

    def map[A, B](fa: F[A])(f: A => B): F[B] = ap(pure(f))(fa)
  }

  trait FlatMap[F[_]] extends Apply[F] {
    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  }

  trait Monad[F[_]] extends FlatMap[F] with Applicative[F]

  pureTest("Functor provides map") {
    object Functor {
      @inline def apply[F[_]](implicit instance: Functor[F]): Functor[F] = instance
    }

    @nowarn
    implicit val functorForOption: Functor[Option] = new Functor[Option] {
      override def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa match {
        case None    => None
        case Some(a) => Some(f(a))
      }
    }

    expect(true)
  }
}
