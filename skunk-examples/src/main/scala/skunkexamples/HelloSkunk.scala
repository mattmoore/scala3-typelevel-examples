package skunkexamples

import cats.effect.*
import skunk.*
import skunk.implicits.*
import skunk.codec.all.*
import org.typelevel.otel4s.trace.Tracer.Implicits.noop

object HelloSkunk extends IOApp {
  val session: Resource[IO, Session[IO]] =
    Session.single(
      host = "localhost",
      port = 5432,
      user = "jimmy",
      database = "world",
      password = Some("banana"),
    )

  def run(args: List[String]): IO[ExitCode] =
    session.use { s =>
      for {
        d <- s.unique(sql"select current_date".query(date))
        _ <- IO.println(s"The current date is $d.")
      } yield ExitCode.Success
    }
}
