import sbt.*

object Dependencies {
  object Projects {
    lazy val typelevelExamples =
      cats ++
        weaver

    lazy val skunkExamples =
      cats ++
        fs2 ++
        natchez ++
        skunk ++
        sourcepos ++
        twiddles

    lazy val httpServer =
      Seq(
        catsCore,
        catsEffect,
        catsEffectKernel,
        fs2Io,
        http4sCore,
        http4sCirce,
        http4sDsl,
        http4sServer,
        http4sEmberServer,
      ) ++
        circe ++
        ip4s ++
        logging ++
        skunk ++
        weaver

    lazy val httpServerIt =
      httpServer ++
        testContainers
  }

  lazy val catsCore         = "org.typelevel" %% "cats-core"          % Versions.catsCore
  lazy val catsKernel       = "org.typelevel" %% "cats-kernel"        % Versions.catsCore
  lazy val catsEffect       = "org.typelevel" %% "cats-effect"        % Versions.catsEffect
  lazy val catsEffectStd    = "org.typelevel" %% "cats-effect-std"    % Versions.catsEffect
  lazy val catsEffectKernel = "org.typelevel" %% "cats-effect-kernel" % Versions.catsEffect
  lazy val cats = Seq(
    catsCore,
    catsKernel,
    catsEffect,
    catsEffectStd,
    catsEffectKernel,
  )

  lazy val circe = Seq(
    "io.circe" %% "circe-generic" % Versions.circe,
    "io.circe" %% "circe-literal" % Versions.circe,
  )

  lazy val fs2Core = "co.fs2" %% "fs2-core" % Versions.fs2
  lazy val fs2Io   = "co.fs2" %% "fs2-io"   % Versions.fs2
  lazy val fs2 = Seq(
    fs2Core,
    fs2Io,
  )

  lazy val http4sCore        = "org.http4s" %% "http4s-core"         % Versions.http4s
  lazy val http4sServer      = "org.http4s" %% "http4s-server"       % Versions.http4s
  lazy val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % Versions.http4s
  lazy val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % Versions.http4s
  lazy val http4sDsl         = "org.http4s" %% "http4s-dsl"          % Versions.http4s
  lazy val http4sCirce       = "org.http4s" %% "http4s-circe"        % Versions.http4s
  lazy val http4s = Seq(
    http4sCore,
    http4sServer,
    http4sEmberClient,
    http4sEmberServer,
    http4sDsl,
    http4sCirce,
  )

  lazy val ip4s = Seq(
    "com.comcast" %% "ip4s-core" % Versions.ip4s,
  )

  lazy val log4catsCore = "org.typelevel" %% "log4cats-core"   % Versions.log4cats
  lazy val logback      = "ch.qos.logback" % "logback-classic" % Versions.logback
  lazy val logging = Seq(
    log4catsCore,
    logback,
  )

  lazy val natchez = Seq(
    "org.tpolecat" %% "natchez-core" % Versions.natchez,
  )

  lazy val skunk = Seq(
    "org.tpolecat" %% "skunk-core" % Versions.skunk,
  )

  lazy val sourcepos = Seq(
    "org.tpolecat" %% "sourcepos" % Versions.sourcepos,
  )

  lazy val testContainers = Seq(
    "com.dimafeng"  %% "testcontainers-scala"            % Versions.testContainers,
    "com.dimafeng"  %% "testcontainers-scala-postgresql" % Versions.testContainers,
    "org.postgresql" % "postgresql"                      % Versions.postgres,
  )

  lazy val twiddles = Seq(
    "org.typelevel" %% "twiddles-core" % Versions.twiddles,
  )

  lazy val weaver = Seq(
    "com.disneystreaming" %% "weaver-cats" % Versions.weaver,
  )
}
