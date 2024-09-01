package httpserver.repositories

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import httpserver.domain.Address
import httpserver.domain.GpsCoords
import natchez.Trace
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

trait AddressRepository[F[_]] {
  def getByAddress(address: Address): F[Option[Address]]
}

object AddressRepository {
  def apply[F[_]: Async: Network: Console: Trace](
      host: String,
      port: Int,
      username: String,
      password: String,
      database: String,
  ): AddressRepository[F] = new AddressRepository[F] {
    val session: Resource[F, Session[F]] = Session.single(
      host = host,
      port = port,
      user = username,
      password = Some(password),
      database = database,
    )

    val addressDecoder: Decoder[Address] =
      (int4 ~ varchar ~ varchar ~ varchar ~ numeric(50, 20) ~ numeric(50, 20))
        .map { case id ~ street ~ city ~ state ~ lat ~ lon =>
          Address(id, street, city, state, GpsCoords(lat, lon))
        }

    override def getByAddress(address: Address): F[Option[Address]] =
      session.use { s =>
        for {
          result <- s.execute(sql"SELECT id, street, city, state, lat, lon FROM addresses".query(addressDecoder))
        } yield result.headOption
      }
  }
}
