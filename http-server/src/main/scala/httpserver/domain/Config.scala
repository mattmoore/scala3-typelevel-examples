package httpserver.domain

import cats.effect.kernel.Async
import cats.syntax.all.*
import ciris.*

final case class DatabaseConfig(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String,
)

final case class Config(
    port: Int,
    databaseConfig: DatabaseConfig,
)

val databaseConfig: ConfigValue[Effect, DatabaseConfig] =
  (
    env("DB_HOST").as[String].default("localhost"),
    env("DB_PORT").as[Int].default(5432),
    env("DB_USERNAME").as[String].default("scala"),
    env("DB_PASSWORD").as[String].default("scala"),
    env("DB_DATABASE").as[String].default("geolocation"),
  ).parMapN(DatabaseConfig.apply)

val config: ConfigValue[Effect, Config] =
  (
    env("PORT").as[Int].default(8080),
    databaseConfig,
  ).parMapN(Config.apply)

object Config {
  def load[F[_]: Async]: F[Config] =
    config.load
}
