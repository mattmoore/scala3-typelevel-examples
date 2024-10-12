package graphs
package socialgraph

import scalax.collection.OneOrMore
import scalax.collection.generic.AbstractDiEdge
import scalax.collection.generic.AbstractUnDiEdge
import scalax.collection.generic.AnyEdge
import scalax.collection.generic.MultiEdge
import scalax.collection.immutable.*

type SocialGraph = Graph[Person, Relation]

object SocialGraph extends TypedGraphFactory[Person, Relation]

case class Person(name: String)

sealed trait Relation extends AnyEdge[Person]

case class Parent(
    child: Person,
    parent: Person,
) extends AbstractDiEdge(
      source = child,
      target = parent,
    )
    with Relation

case class Child(
    parent: Person,
    child: Person,
) extends AbstractDiEdge(
      source = parent,
      target = child,
    )
    with Relation

case class Siblings(
    one: Person,
    another: Person,
) extends AbstractUnDiEdge(
      source = one,
      target = another,
    )
    with MultiEdge
    with Relation {
  def extendKeyBy = OneOrMore.one(Siblings)
}

case class Friends(
    one: Person,
    another: Person,
) extends AbstractUnDiEdge(
      source = one,
      target = another,
    )
    with MultiEdge
    with Relation {
  def extendKeyBy = OneOrMore.one(Friends)
}

extension (graph: SocialGraph)
  def allSuccessors(person: Person): Set[graph.NodeT] =
    val personNode = graph.get(person)
    personNode.withSubgraph().toSet - personNode

  def parents(child: Person): Set[graph.GraphInnerEdge] =
    graph
      .get(child)
      .edges
      .filter(_.outer.isInstanceOf[Parent])
      .toSet

  def siblings(child: Person): Set[graph.NodeT] =
    graph
      .get(child)
      .diPredecessors
      .toSet
