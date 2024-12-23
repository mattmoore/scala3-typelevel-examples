import weaver.*

import cats.effect.IO

object ConfigServiceSuite extends SimpleIOSuite {
  test("Merging environment configs") {
    val configService: ConfigService[IO] = ConfigService[IO]

    for {
      mergedConfigFoldStrategy      <- configService.getMergedConfigFoldStrategy
      mergedConfigSemigroupStrategy <- configService.getMergedConfigSemigroupStrategy
    } yield expect.all(
      mergedConfigFoldStrategy == Config(
        env = "prod",
        user = "produser",
        password = "prodpass",
        debugOptions = List(
          DebugOption("logging", "enabled"),
          DebugOption("PROD", "true"),
        ),
      ),
      mergedConfigSemigroupStrategy == Config(
        env = "prod",
        user = "produser",
        password = "prodpass",
        debugOptions = List(
          DebugOption("logging", "enabled"),
          DebugOption("PROD", "true"),
        ),
      ),
    )
  }
}
