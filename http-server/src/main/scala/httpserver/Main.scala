package httpserver

import cats.*
import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp

object Main extends ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] =
    for {
      resources <- Resources.make[IO]
      _         <- resources.httpServer
    } yield ()
}
