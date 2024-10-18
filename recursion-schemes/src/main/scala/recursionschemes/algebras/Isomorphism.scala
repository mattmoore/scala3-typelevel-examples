package recursionschemes.algebras

trait Isomorphism[Arrow[_, _], A, B] { self =>
  def forward: Arrow[A, B]
  def inverse: Arrow[B, A]
}

type IsoSet[A, B] = Isomorphism[Function1, A, B]
type <=>[A, B]    = IsoSet[A, B]
