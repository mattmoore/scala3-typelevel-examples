package compiler

import parser.*
import weaver.*

object WorkflowCompilerSuite extends SimpleIOSuite {
  pureTest("Compiler transforms source to AST") {
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

    val ast = WorkflowCompiler(source)

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
