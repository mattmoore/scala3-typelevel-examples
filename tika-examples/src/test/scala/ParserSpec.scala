import weaver.*

import cats.effect.*

object ParserSpec extends SimpleIOSuite {
  test("Tika Parser") {
    val parser      = Parser()
    val inputStream = getClass().getResourceAsStream("file.odt")

    for {
      parseResults <- parser.parse(inputStream)
      _ <- IO.println(s"CONTENT:\n${parseResults.handler.toString()}")
      _ <- IO.println(s"METADATA: ${parseResults.metadata}")
      _ <- IO.println(s"METADATA PARAGRAPH COUNT: ${parseResults.metadata.get("meta:paragraph-count")}")
      _ <- IO.println(s"METADATA WORD COUNT: ${parseResults.metadata.get("meta:word-count")}")
      _ <- IO.println(s"METADATA CHARACTER COUNT: ${parseResults.metadata.get("meta:character-count")}")
      _ <- IO.println(s"METADATA TABLE COUNT: ${parseResults.metadata.get("meta:table-count")}")
    } yield expect.all(
      parseResults.handler.toString.trim == "Matt is a software engineer, born 1987.",
    )
  }
}
