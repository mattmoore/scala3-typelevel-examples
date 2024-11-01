import org.apache.jena.graph.Triple
import org.apache.jena.query.*
import org.apache.jena.sparql.algebra.Algebra
import org.apache.jena.sparql.algebra.Op
import org.apache.jena.sparql.algebra.OpAsQuery
import org.apache.jena.sparql.algebra.op.OpBGP
import org.apache.jena.sparql.core.BasicPattern
import org.apache.jena.sparql.core.Var
import weaver.*

import scala.jdk.CollectionConverters.*

object ParserSuite extends SimpleIOSuite {
  pureTest("Query string source to Op") {
    val source =
      """|SELECT * { ?s ?p ?o }
         |""".stripMargin

    val query: Query = QueryFactory.create(source)
    val op: Op       = Algebra.compile(query)

    val expected: OpBGP = OpBGP(
      BasicPattern.wrap(
        List(
          Triple.create(Var.alloc("s"), Var.alloc("p"), Var.alloc("o")),
        ).asJava,
      ),
    )

    expect.all(
      op == expected,
    )
  }

  pureTest("Op back to Query string source") {
    val op: OpBGP = OpBGP(
      BasicPattern.wrap(
        List(
          Triple.create(Var.alloc("s"), Var.alloc("p"), Var.alloc("o")),
        ).asJava,
      ),
    )

    val query: Query = OpAsQuery.asQuery(op)

    val expected =
      """|SELECT  *
         |WHERE
         |  { ?s  ?p  ?o }
         |""".stripMargin

    val actual = query.serialize

    println(s"EXPECTED:\n${expected}")
    println(s"ACTUAL:\n${actual}")
    println(s"DIFF:\n${diff(actual, expected)}")

    expect.all(
      actual == expected,
    )
  }

  def diff(s1: String, s2: String): List[String] =
    s1.lazyZip(s2)
      .collect {
        case (x, y) if x != y => s"$x != $y"
      }
      .toList ++
      s1.drop(s2.length).map(x => s"$x is undefined") ++
      s2.drop(s1.length).map(y => s"$y is missing")
}
