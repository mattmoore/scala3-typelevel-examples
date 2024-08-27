package httpserver

import cats.effect.*

object Server extends ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] =
    for {
      resources <- Resources.make
      _         <- resources.httpServer
    } yield ()
}
