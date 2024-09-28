package lexer

import weaver.*

object WorkflowLexerSuite extends SimpleIOSuite {
  pureTest("Lexer converts source to sequence of tokens") {
    val source =
      s"""|read input name, country
          |switch:
          |  country == "PT" ->
          |    call service "A"
          |    exit
          |  otherwise ->
          |    call service "B"
          |    switch:
          |      name == "unknown" ->
          |        exit
          |      otherwise ->
          |        call service "C"
          |        exit
          |""".stripMargin

    val result = WorkflowLexer(source)

    expect(
      result == Right(
        List(
          READINPUT(),
          IDENTIFIER("name"),
          COMMA(),
          IDENTIFIER("country"),
          SWITCH(),
          COLON(),
          INDENT(),
          IDENTIFIER("country"),
          EQUALS(),
          LITERAL("PT"),
          ARROW(),
          INDENT(),
          CALLSERVICE(),
          LITERAL("A"),
          EXIT(),
          DEDENT(),
          OTHERWISE(),
          ARROW(),
          INDENT(),
          CALLSERVICE(),
          LITERAL("B"),
          SWITCH(),
          COLON(),
          INDENT(),
          IDENTIFIER("name"),
          EQUALS(),
          LITERAL("unknown"),
          ARROW(),
          INDENT(),
          EXIT(),
          DEDENT(),
          OTHERWISE(),
          ARROW(),
          INDENT(),
          CALLSERVICE(),
          LITERAL("C"),
          EXIT(),
          DEDENT(),
          DEDENT(),
          DEDENT(),
          DEDENT(),
        ),
      ),
    )
  }
}
