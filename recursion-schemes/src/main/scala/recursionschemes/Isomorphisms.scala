package recursionschemes

trait Isomorphism[Arrow[_, _], A, B] { self =>
  def to: Arrow[A, B]
  def from: Arrow[B, A]
}

type IsoSet[A, B] = Isomorphism[Function1, A, B]
type <=>[A, B]    = IsoSet[A, B]
