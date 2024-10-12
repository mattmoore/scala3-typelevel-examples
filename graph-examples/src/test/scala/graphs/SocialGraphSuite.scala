package graphs

import graphs.SocialGraph.*
import scalax.collection.immutable.Graph
import weaver.*

object SocialGraphSuite extends SimpleIOSuite {
  pureTest("Scala Graph") {
    val matt    = Person("Matt")
    val mary    = Person("Mary")
    val raphael = Person("Raphael")
    val kira    = Person("Kira")

    val graph: Graph[Person, Relation] = SocialGraph.from(
      Parent(raphael, matt)
        :: Parent(raphael, mary)
        :: Siblings(raphael, kira)
        :: Nil,
    )

    println(graph)
    println(allSuccessors(graph)(raphael))
    println(parents(graph)(raphael))

    expect.all(
      graph == SocialGraph.from(Parent(raphael, matt) :: Parent(raphael, mary) :: Siblings(raphael, kira) :: Nil),
      allSuccessors(graph)(raphael) == Set(matt, kira, mary),
      parents(graph)(raphael) == Set(Parent(raphael, matt), Parent(raphael, mary)),
    )
  }
}
