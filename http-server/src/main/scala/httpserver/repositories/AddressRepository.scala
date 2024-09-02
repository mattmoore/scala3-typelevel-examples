package httpserver.repositories

import cats.effect.*
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import httpserver.domain.Address
import httpserver.domain.GpsCoords
import httpserver.domain.*
import natchez.Trace
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

trait AddressRepository[F[_]] {
  def getByAddress(addressQuery: AddressQuery): F[Option[Address]]
}

object AddressRepository {
  def apply[F[_]: Async: Network: Console: Trace]()(using
      config: Config,
  ): AddressRepository[F] = new AddressRepository[F] {
    val session: Resource[F, Session[F]] = Session.single(
      host = config.databaseConfig.host,
      port = config.databaseConfig.port,
      user = config.databaseConfig.username,
      password = Some(config.databaseConfig.password),
      database = config.databaseConfig.database,
    )

    val codec: Codec[Address] =
      (int4, varchar, varchar, varchar, float8, float8).tupled
        .imap { case (id, street, city, state, lat, lon) =>
          Address(id, street, city, state, GpsCoords(lat, lon))
        } { case Address(id, street, city, state, coords) =>
          (id, street, city, state, coords.lat, coords.lon)
        }

    override def getByAddress(query: AddressQuery): F[Option[Address]] =
      val fragment: Query[(String, String), Address] =
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
              |""".stripMargin.query(codec).to[Address]
      session.use { s =>
        for {
          statement <- s.prepare(fragment)
          result    <- statement.stream((query.city, query.state), 16).compile.toList
        } yield result.headOption
      }
  }
}
