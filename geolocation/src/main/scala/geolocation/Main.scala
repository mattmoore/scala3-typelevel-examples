package geolocation

import cats.*
import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp
import geolocation.domain.Config
import geolocation.http.ServerResource
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import org.typelevel.log4cats.Logger

object Main extends ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] = {
    for {
      resources <- Resources.make[IO]
      given Config                 = resources.config
      given Logger[IO]             = resources.logger
      given HelloService[IO]       = resources.helloService
      given GeolocationService[IO] = GeolocationService[IO](resources.addressRepo)
      _ <- ServerResource.make[IO]
    } yield ()
  }
}
