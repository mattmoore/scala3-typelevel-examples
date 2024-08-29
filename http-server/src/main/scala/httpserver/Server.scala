package httpserver

import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp

object Server extends ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] =
    for {
      resources <- Resources.make[IO]
      _         <- resources.httpServer
    } yield ()
}
