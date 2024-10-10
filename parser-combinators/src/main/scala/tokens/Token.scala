package tokens

import scala.util.parsing.input.Positional

sealed trait Token extends Positional

case class IDENTIFIER(str: String)  extends Token
case class LITERAL(str: String)     extends Token
case class INDENTATION(spaces: Int) extends Token
case class EXIT()                   extends Token
case class READINPUT()              extends Token
case class CALLSERVICE()            extends Token
case class SWITCH()                 extends Token
case class OTHERWISE()              extends Token
case class COLON()                  extends Token
case class ARROW()                  extends Token
case class EQUALS()                 extends Token
case class COMMA()                  extends Token
case class INDENT()                 extends Token
case class DEDENT()                 extends Token
