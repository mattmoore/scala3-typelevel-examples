package graphs

import graphs.socialgraph.*
import weaver.*

object SocialGraphSuite extends SimpleIOSuite {
  pureTest("Scala Graph") {
    val matt      = Person("Matt")
    val mary      = Person("Mary")
    val raphael   = Person("Raphael")
    val kira      = Person("Kira")
    val christina = Person("Christina")

    val john   = Person("John")
    val julia  = Person("Julia")
    val robert = Person("Robert")
    val maria  = Person("Maria")

    val graph: SocialGraph = SocialGraph.from(
      List(
        Parent(raphael, matt),
        Parent(raphael, mary),
        Siblings(raphael, kira),
        Siblings(raphael, christina),
        Parent(robert, john),
        Parent(robert, julia),
        Siblings(robert, maria),
      ),
    )

    println(s"GRAPH: $graph")

    (
      s"RAPHAEL'S SUCCESSORS: ${graph.allSuccessors(raphael)}",
      s"RAPHAEL'S PARENTS: ${graph.parents(raphael)}",
      s"RAPHAEL'S SIBLINGS: ${graph.siblings(raphael)}",
      s"ROBERT'S SUCCESSORS: ${graph.allSuccessors(robert)}",
      s"ROBERT'S PARENTS: ${graph.parents(robert)}",
      s"ROBERT'S SIBLINGS: ${graph.siblings(robert)}",
    ).toList.foreach(println)

    expect.all(
      graph.allSuccessors(raphael) == Set(
        matt,
        mary,
        kira,
        christina,
      ),
      graph.allSuccessors(robert) == Set(
        john,
        julia,
        maria,
      ),
      graph.parents(raphael) == Set(
        Parent(raphael, matt),
        Parent(raphael, mary),
      ),
      graph.parents(robert) == Set(
        Parent(robert, john),
        Parent(robert, julia),
      ),
      graph.siblings(raphael) == Set(
        kira,
        christina,
      ),
      graph.siblings(robert) == Set(
        maria,
      ),
    )
  }
}
