import cats.effect.*
import cats.effect.std.Supervisor
import cats.syntax.all.*

import scala.concurrent.duration.*

def cast[F[_]: Async](x: Int): F[String] =
  for {
    _      <- Async[F].sleep(50.millis)
    result <- Async[F].delay(x.asInstanceOf[String])
  } yield result

object SupervisorErrorHandling extends IOApp.Simple {
  override def run: IO[Unit] =
    Supervisor[IO](await = true).use { supervisor =>
      for {
        _ <- supervisor
          .supervise {
            cast[IO](1)
              .handleErrorWith { e =>
                IO.println(s"ERROR MESSAGE: ${e.getMessage}")
              }
          }
        _ <- IO.sleep(10.millis)
        _ <- IO.println("TEST")
      } yield ()
    }.void
}
