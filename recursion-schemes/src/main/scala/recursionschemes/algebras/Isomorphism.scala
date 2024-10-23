package recursionschemes.algebras

trait Isomorphism[Arrow[_, _], A, B] { self =>

  /** Goes from A => B.
    */
  def forward: Arrow[A, B]

  /** Goes from B => A.
    */
  def inverse: Arrow[B, A]
}

type IsoSet[A, B] = Isomorphism[Function1, A, B]
type <=>[A, B]    = IsoSet[A, B]
