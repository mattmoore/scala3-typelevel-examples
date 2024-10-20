import weaver.*

object SimpleParsingSuite extends SimpleIOSuite {
  pureTest("Simple parsing - function") {
    val source =
      """|def add(x: Int, y: Int) = x + y
         |""".stripMargin

    val tokens = SimpleParser.parse(source)

    expect.all(
      tokens == List(),
    )
  }
}
