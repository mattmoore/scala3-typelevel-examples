import cats.NonEmptyParallel
import cats.Semigroup
import cats.effect.*
import cats.implicits.*

case class DebugOption(key: String, value: String)

case class Config(
    env: String,
    user: String,
    password: String,
    debugOptions: List[DebugOption] = List.empty,
)

trait ConfigService[F[_]] {
  def getDevConfig: F[Config]

  def getTestConfig: F[Config]

  def getProdConfig: F[Config]

  def getMergedConfigFoldStrategy: F[Config]

  def getMergedConfigSemigroupStrategy: F[Config]
}

object ConfigService {
  def apply[F[_]: Async: NonEmptyParallel]: ConfigService[F] =
    new ConfigService {
      override def getDevConfig: F[Config] =
        Async[F].pure(
          Config(
            env = "dev",
            user = "devuser",
            password = "devpass",
            debugOptions = List(
              DebugOption("logging", "enabled"),
            ),
          ),
        )

      override def getTestConfig: F[Config] =
        Async[F].pure(
          Config(
            env = "test",
            user = "testuser",
            password = "testpass",
          ),
        )

      override def getProdConfig: F[Config] =
        Async[F].pure(
          Config(
            env = "prod",
            user = "produser",
            password = "prodpass",
            debugOptions = List(
              DebugOption("PROD", "true"),
            ),
          ),
        )

      override def getMergedConfigFoldStrategy: F[Config] =
        (getDevConfig, getTestConfig, getProdConfig).parMapN { (devConfig, testConfig, prodConfig) =>
          List(testConfig, prodConfig).foldLeft(devConfig) { (mergedConfig, nextConfig) =>
            mergedConfig.copy(
              env = nextConfig.env,
              user = nextConfig.user,
              password = nextConfig.password,
              debugOptions = mergedConfig.debugOptions ++ nextConfig.debugOptions,
            )
          }
        }

      override def getMergedConfigSemigroupStrategy: F[Config] = {
        given Semigroup[Config] = new Semigroup[Config] {
          override def combine(x: Config, y: Config): Config =
            Config(
              env = y.env,
              user = y.user,
              password = y.password,
              debugOptions = x.debugOptions ++ y.debugOptions,
            )
        }

        (getDevConfig, getTestConfig, getProdConfig).parMapN { (devConfig, testConfig, prodConfig) =>
          devConfig |+| testConfig |+| prodConfig
        }
      }
    }
}
