import scalax.collection.OneOrMore
import scalax.collection.generic.AbstractDiEdge
import scalax.collection.generic.AbstractUnDiEdge
import scalax.collection.generic.AnyEdge
import scalax.collection.generic.MultiEdge
import scalax.collection.immutable.TypedGraphFactory
import weaver.*

object ScalaGraphSuite extends SimpleIOSuite {
  pureTest("Scala Graph") {
    case class Person(name: String)

    sealed trait Relation                            extends AnyEdge[Person]
    case class Parent(child: Person, parent: Person) extends AbstractDiEdge(source = child, target = parent) with Relation
    case class Child(parent: Person, child: Person)  extends AbstractDiEdge(source = parent, target = child) with Relation
    case class Friends(one: Person, another: Person) extends AbstractUnDiEdge(source = one, target = another) with MultiEdge with Relation {
      def extendKeyBy = OneOrMore.one(Friends)
    }
    case class Siblings(one: Person, another: Person) extends AbstractUnDiEdge(source = one, target = another) with MultiEdge with Relation {
      def extendKeyBy = OneOrMore.one(Siblings)
    }

    val matt    = Person("Matt")
    val mary    = Person("Mary")
    val raphael = Person("Raphael")
    val kira    = Person("Kira")

    object SocialGraph extends TypedGraphFactory[Person, Relation]

    val graph = SocialGraph.from(Parent(raphael, matt) :: Parent(raphael, mary) :: Siblings(raphael, kira) :: Nil)
    println(graph)

    val raphaelNode = graph.get(raphael)

    val subgraph      = raphaelNode.withSubgraph().toSet
    val allSuccessors = subgraph - raphaelNode
    println(allSuccessors)

    val parents = raphaelNode.edges.filter(_.outer.isInstanceOf[Parent])
    println(parents)

    expect.all(
      graph == SocialGraph.from(Parent(raphael, matt) :: Parent(raphael, mary) :: Siblings(raphael, kira) :: Nil),
      allSuccessors == Set(matt, kira, mary),
      parents == Set(Parent(raphael, matt), Parent(raphael, mary)),
    )
  }
}
