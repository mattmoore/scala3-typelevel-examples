package graphs

import graphs.SocialGraph.Person
import graphs.SocialGraph.Relation
import scalax.collection.OneOrMore
import scalax.collection.generic.AbstractDiEdge
import scalax.collection.generic.AbstractUnDiEdge
import scalax.collection.generic.AnyEdge
import scalax.collection.generic.MultiEdge
import scalax.collection.immutable.*

object SocialGraph extends TypedGraphFactory[Person, Relation] {
  type SocialGraph = TypedGraphFactory[Person, Relation]

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

  def allSuccessors(graph: Graph[Person, Relation])(person: Person): Set[graph.NodeT] = {
    val personNode = graph.get(person)
    personNode.withSubgraph().toSet - personNode
  }

  def parents(graph: Graph[Person, Relation])(child: Person): Set[graph.GraphInnerEdge] =
    graph.get(child).edges.filter(_.outer.isInstanceOf[Parent]).toSet
}
