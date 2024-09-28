package compiler

import lexer.*
import parser.*

object WorkflowCompiler {
  def apply(code: String): Either[WorkflowCompilationError, WorkflowAST] = {
    for {
      tokens <- WorkflowLexer(code)
      ast    <- WorkflowParser(tokens)
    } yield ast
  }
}
