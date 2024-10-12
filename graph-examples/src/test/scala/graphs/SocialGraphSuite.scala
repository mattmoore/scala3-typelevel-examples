package graphs

import graphs.socialgraph.*
import weaver.*

object SocialGraphSuite extends SimpleIOSuite {
  pureTest("Scala Graph") {
    val matt    = Person("Matt")
    val mary    = Person("Mary")
    val raphael = Person("Raphael")
    val kira    = Person("Kira")
    val maria   = Person("Maria")

    val graph: SocialGraph = SocialGraph.from(
      List(
        Parent(raphael, matt),
        Parent(raphael, mary),
        Siblings(raphael, kira),
        Siblings(raphael, maria),
      ),
    )

    (
      graph,
      graph.allSuccessors(raphael),
      graph.parents(raphael),
      graph.siblings(raphael),
    ).toList.foreach(println)

    expect.all(
      graph.allSuccessors(raphael) == Set(matt, mary, kira, maria),
      graph.parents(raphael) == Set(Parent(raphael, matt), Parent(raphael, mary)),
    )
  }
}
