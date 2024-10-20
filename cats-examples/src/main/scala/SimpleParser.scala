object SimpleParser {
  sealed trait Token
  case object DEFN extends Token

  def parse(source: String): List[Token] =
    source.foldLeft(List.empty) { (tokens, nextChar) =>
      List.empty
    }
}
