package parser

import lexer.*
import weaver.*

object WorkflowParserSuite extends SimpleIOSuite {
  pureTest("Parser transforms token sequence to AST") {
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

    val tokens = WorkflowLexer(source).getOrElse(List.empty)
    val ast    = WorkflowParser(tokens)

    expect(
      ast == Right(
        AndThen(
          ReadInput(List("name", "country")),
          Choice(
            List(
              IfThen(
                Equals("country", "PT"),
                AndThen(CallService("A"), Exit),
              ),
              OtherwiseThen(
                AndThen(
                  CallService("B"),
                  Choice(
                    List(
                      IfThen(Equals("name", "unknown"), Exit),
                      OtherwiseThen(AndThen(CallService("C"), Exit)),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    )
  }
}
