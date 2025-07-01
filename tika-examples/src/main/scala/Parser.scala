import cats.effect.*
import org.apache.tika.config.TikaConfig
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler

import java.io.InputStream

case class ParseResults(
    metadata: Metadata,
    handler: BodyContentHandler,
)

trait Parser {
  def parse(inputStream: InputStream): IO[ParseResults]
}

object Parser {
  def apply(): Parser = new Parser {
    override def parse(stream: InputStream): IO[ParseResults] = IO {
      val tikaConfig = TikaConfig.getDefaultConfig()
      val parser     = AutoDetectParser(tikaConfig)
      val metadata   = Metadata()
      val handler    = BodyContentHandler()
      parser.parse(stream, handler, metadata, ParseContext())
      ParseResults(metadata, handler)
    }
  }
}
