package lexer

import scala.util.parsing.combinator.RegexParsers
import compiler.WorkflowLexerError
import tokens.*

trait Lexer extends RegexParsers {
  def apply(code: String): Either[WorkflowLexerError, List[Token]]
}

object Lexer extends Lexer {
  def apply(code: String): Either[WorkflowLexerError, List[Token]] = {
    Right(List.empty)
  }

  def tokens: Parser[List[Token]] = {
    phrase(
      rep1(
        identifier | indentation,
      ),
    ) ^^ { rawTokens =>
      processIndentations(rawTokens)
    }
  }

  private def processIndentations(tokens: List[Token], indents: List[Int] = List(0)): List[Token] = {
    tokens.headOption match {

      // if there is an increase in indentation level, we push this new level into the stack
      // and produce an INDENT
      case Some(INDENTATION(spaces)) if spaces > indents.head =>
        INDENT() :: processIndentations(tokens.tail, spaces :: indents)

      // if there is a decrease, we pop from the stack until we have matched the new level and
      // we produce a DEDENT for each pop
      case Some(INDENTATION(spaces)) if spaces < indents.head =>
        val (dropped, kept) = indents.partition(_ > spaces)
        (dropped map (_ => DEDENT())) ::: processIndentations(tokens.tail, kept)

      // if the indentation level stays unchanged, no tokens are produced
      case Some(INDENTATION(spaces)) if spaces == indents.head =>
        processIndentations(tokens.tail, indents)

      // other tokens are ignored
      case Some(token) =>
        token :: processIndentations(tokens.tail, indents)

      // the final step is to produce a DEDENT for each indentation level still remaining, thus
      // "closing" the remaining open INDENTS
      case None =>
        indents.filter(_ > 0).map(_ => DEDENT())

    }
  }

  def identifier: Parser[IDENTIFIER] = positioned {
    "val [a-zA-Z_][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
  }

  def literal: Parser[LITERAL] = positioned {
    """"[^"]*"""".r ^^ { str =>
      val content = str.substring(1, str.length - 1)
      LITERAL(content)
    }
  }

  def indentation: Parser[INDENTATION] = positioned {
    "\n[ ]*".r ^^ { whitespace =>
      val nSpaces = whitespace.length - 1
      INDENTATION(nSpaces)
    }
  }

  def exit        = positioned { "exit" ^^ (_ => EXIT()) }
  def readInput   = positioned { "read input" ^^ (_ => READINPUT()) }
  def callService = positioned { "call service" ^^ (_ => CALLSERVICE()) }
  def switch      = positioned { "switch" ^^ (_ => SWITCH()) }
  def otherwise   = positioned { "otherwise" ^^ (_ => OTHERWISE()) }
  def colon       = positioned { ":" ^^ (_ => COLON()) }
  def arrow       = positioned { "->" ^^ (_ => ARROW()) }
  def equals      = positioned { "==" ^^ (_ => EQUALS()) }
  def comma       = positioned { "," ^^ (_ => COMMA()) }
}
