package geolocation

import cats.*
import cats.effect.IO
import cats.effect.Resource
import cats.effect.ResourceApp
import geolocation.domain.Config
import geolocation.http.ServerResource
import geolocation.repositories.AddressRepository
import geolocation.services.GeolocationService
import geolocation.services.HelloService
import natchez.Trace.Implicits.noop
import org.typelevel.log4cats.Logger
import skunk.Session

object Main extends ResourceApp.Forever {
  def run(args: List[String]): Resource[IO, Unit] = {
    for {
      resources <- Resources.make[IO]
      given Config                               = resources.config
      given Logger[IO]                           = resources.logger
      given HelloService[IO]                     = HelloService[IO]()
      given Session[IO]                          = resources.dbSession
      given AddressRepository[IO]                = AddressRepository[IO]()
      geolocationService: GeolocationService[IO] = GeolocationService()
      given GeolocationService[IO]               = geolocationService
      _ <- ServerResource.make[IO]
    } yield ()
  }
}
