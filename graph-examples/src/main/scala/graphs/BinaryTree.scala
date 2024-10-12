package graphs.binarytree

sealed trait Tree

case class Node(
    left: Tree,
    value: Int,
    right: Tree,
) extends Tree

case object Leaf extends Tree

/** TreeF for recursion scheme
  */
sealed trait TreeF[+A]

case class NodeF[A](
    left: A,
    value: Int,
    right: A,
) extends TreeF[A]

case object LeafF extends TreeF[Nothing]

def projectTree: Tree => TreeF[Tree] = {
  case Node(l, v, r) => NodeF(l, v, r)
  case Leaf          => LeafF
}

trait Functor[F[_]] {
  def map[A, B](fa: F[A], f: A => B): F[B]
}

implicit val treeFFunctor: Functor[TreeF] = new Functor[TreeF] {
  override def map[A, B](tree: TreeF[A], f: A => B) =
    tree match {
      case NodeF(left, value, right) => NodeF(f(left), value, f(right))
      case LeafF                     => LeafF
    }
}

val heightAlgebra: TreeF[Int] => Int = {
  case NodeF(left, _, right) => 1 + math.max(left, right)
  case LeafF                 => 0
}

def map[F[_], A, B](
    fa: F[A],
    f: A => B,
)(implicit
    functor: Functor[F],
): F[B] =
  functor.map(fa, f)

def cata[F[_]: Functor, A, B](
    algebra: F[A] => A,
    projection: B => F[B],
): B => A = {
  def loop(state: B): A =
    algebra(map(projection(state), loop))

  loop
}

val height: Tree => Int =
  cata(heightAlgebra, projectTree)
