package lexer

import weaver.*

object LexerSuite extends SimpleIOSuite {
  pureTest("") {
    expect(true)
  }

  // pureTest("Lexer converts source to sequence of tokens") {
  //   val source =
  //     s"""|val x = 1
  //         |""".stripMargin

  //   val result = Lexer(source)

  //   expect(
  //     result == Right(
  //       List(
  //         IDENTIFIER("x"),
  //       ),
  //     ),
  //   )
  // }
}
