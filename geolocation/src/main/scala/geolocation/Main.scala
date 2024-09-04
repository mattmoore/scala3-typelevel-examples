package geolocation

import cats.effect.*

// Can run this as a ResourceApp.Forever with a for comprehension
object Main extends ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] = for {
    _ <- Resources.make[IO]
  } yield ()
}

// Can run this as a ResourceApp.Forever with a map
// object Main extends ResourceApp.Forever {
//   def run(args: List[String]): Resource[IO, Unit] =
//     Resources.make[IO].map(_ => ())
// }

// Can run this as an IOApp with a useForever
// object Main extends IOApp {
//   def run(args: List[String]): IO[ExitCode] =
//     Resources.make[IO].useForever.as(ExitCode.Success)
// }
