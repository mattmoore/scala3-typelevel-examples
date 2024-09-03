package geolocation.repositories

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import geolocation.domain.Address
import geolocation.domain.GpsCoords
import geolocation.domain.*
import natchez.Trace
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

trait AddressRepository[F[_]] {
  def getByAddress(addressQuery: AddressQuery): F[Option[Address]]

  def insert(address: Address): F[Either[String, Unit]]
}

object AddressRepository {
  case class AddressRow(
      id: Int,
      street: String,
      city: String,
      state: String,
      lat: Double,
      lon: Double,
  )

  object AddressRow {
    def fromDomain(address: Address): AddressRow = AddressRow(
      id = address.id,
      street = address.street,
      city = address.city,
      state = address.state,
      lat = address.coords.lat,
      lon = address.coords.lon,
    )
  }

  val addressCodec: Codec[Address] =
    (int4, varchar, varchar, varchar, float8, float8).tupled
      .imap { case (id, street, city, state, lat, lon) =>
        Address(id, street, city, state, GpsCoords(lat, lon))
      } { case Address(id, street, city, state, coords) =>
        (id, street, city, state, coords.lon, coords.lat)
      }

  val addressRowCodec: Codec[AddressRow] =
    (int4, varchar, varchar, varchar, float8, float8).tupled
      .imap { case (id, street, city, state, lon, lat) =>
        AddressRow(id, street, city, state, lon, lat)
      } { case AddressRow(id, street, city, state, lat, lon) =>
        (id, street, city, state, lon, lat)
      }

  def apply[F[_]: Async: Network: Console: Trace]()(using
      config: Config,
      session: Session[F],
  ): AddressRepository[F] = new AddressRepository[F] {
    override def getByAddress(query: AddressQuery): F[Option[Address]] = {
      val getByAddressQuery: Query[(String, String), Address] =
        sql"""|SELECT
              |  id,
              |  street,
              |  city,
              |  state,
              |  ST_Y(coords) AS lat,
              |  ST_X(coords) AS lon
              |FROM addresses
              |WHERE city LIKE $varchar
              |  AND state LIKE $varchar
              |""".stripMargin.query(addressCodec).to[Address]
      for {
        statement <- session.prepare(getByAddressQuery)
        result    <- statement.stream((query.city, query.state), 16).compile.toList
      } yield result.headOption
    }

    override def insert(address: Address): F[Either[String, Unit]] = {
      val insertCommand: Command[AddressRow] =
        sql"""|INSERT INTO addresses(
              |  id,
              |  street,
              |  city,
              |  state,
              |  coords
              |) VALUES (
              |  $int4,
              |  $varchar,
              |  $varchar,
              |  $varchar,
              |  ST_SetSRID(ST_MakePoint($float8, $float8), 4326)
              |)
              |""".stripMargin.command.to[AddressRow]
      for {
        statement <- session.prepare(insertCommand)
        result <- statement
          .execute(AddressRow.fromDomain(address))
          .flatMap(_ => Right(()).pure)
          .handleErrorWith(_ => Left("Unable to save address").pure)
      } yield result
    }
  }
}
