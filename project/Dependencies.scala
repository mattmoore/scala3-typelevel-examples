import sbt.*

object Dependencies {
  object Projects {
    lazy val typelevelExamples =
      cats ++
        droste ++
        ducktape ++
        weaver ++
        scalagraph

    lazy val fs2Examples =
      cats ++
        fs2 ++
        weaver

    lazy val skunkExamples =
      cats ++
        fs2 ++
        natchez ++
        skunk ++
        sourcepos ++
        twiddles

    lazy val parserCombinators =
      cats ++
        Seq(scalaParserCombinators) ++
        weaver

    lazy val jenaExamples =
      cats ++
        jena ++
        droste ++
        ducktape ++
        weaver

    lazy val tapirExamples =
      cats ++
        droste ++
        ducktape ++
        http4s ++
        prometheus ++
        scalagraph ++
        tapir ++
        weaver
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

  lazy val ciris = Seq(
    "is.cir" %% "ciris" % Versions.ciris,
  )

  lazy val droste = Seq(
    "io.higherkindness" %% "droste-core"   % Versions.droste,
    "io.higherkindness" %% "droste-macros" % Versions.droste,
  )

  lazy val ducktape = Seq(
    "io.github.arainko" %% "ducktape" % Versions.ducktape,
  )

  lazy val flyway = Seq(
    "org.flywaydb" % "flyway-database-postgresql" % Versions.flyway,
    postgresql,
  )

  lazy val fs2Core            = "co.fs2" %% "fs2-core"             % Versions.fs2
  lazy val fs2Io              = "co.fs2" %% "fs2-io"               % Versions.fs2
  lazy val fs2ReactiveStreams = "co.fs2" %% "fs2-reactive-streams" % Versions.fs2
  lazy val fs2Codec           = "co.fs2" %% "fs2-scodec"           % Versions.fs2
  lazy val fs2 = Seq(
    fs2Core,
    fs2Io,
    fs2ReactiveStreams,
    fs2Codec,
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

  lazy val jenaArq = "org.apache.jena" % "jena-arq" % Versions.jena
  lazy val jena = Seq(
    jenaArq,
  )

  lazy val log4catsCore           = "org.typelevel"       %% "log4cats-core"            % Versions.log4cats
  lazy val logback                = "ch.qos.logback"       % "logback-classic"          % Versions.logback
  lazy val logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % Versions.logstashLogbackEncoder
  lazy val logging = Seq(
    log4catsCore,
    logback,
    logstashLogbackEncoder,
  )

  lazy val natchez = Seq(
    "org.tpolecat" %% "natchez-core" % Versions.natchez,
  )

  lazy val otel4s = Seq(
    "org.typelevel"   %% "otel4s-oteljava"                           % Versions.otel4s,
    "io.opentelemetry" % "opentelemetry-exporter-otlp"               % Versions.opentelemetry % Runtime,
    "io.opentelemetry" % "opentelemetry-exporter-logging"            % Versions.opentelemetry % Runtime,
    "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % Versions.opentelemetry % Runtime,
  )

  lazy val scalagraphCore        = "org.scala-graph" %% "graph-core"        % Versions.scalagraphCore cross CrossVersion.for3Use2_13
  lazy val scalagraphDot         = "org.scala-graph" %% "graph-dot"         % Versions.scalagraphDot cross CrossVersion.for3Use2_13
  lazy val scalagraphJson        = "org.scala-graph" %% "graph-json"        % Versions.scalagraphJson cross CrossVersion.for3Use2_13
  lazy val scalagraphConstrained = "org.scala-graph" %% "graph-constrained" % Versions.scalagraphConstrained cross CrossVersion.for3Use2_13
  lazy val scalagraph = Seq(
    scalagraphCore,
    scalagraphDot,
    scalagraphJson,
    scalagraphConstrained,
  )

  lazy val scalaParserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Versions.scalaParserCombinators

  lazy val postgresql = "org.postgresql" % "postgresql" % Versions.postgres

  lazy val prometheusMetricsCore  = "io.prometheus" % "prometheus-metrics-core"  % Versions.prometheus
  lazy val prometheusMetricsModel = "io.prometheus" % "prometheus-metrics-model" % Versions.prometheus
  lazy val prometheus = Seq(
    prometheusMetricsCore,
    prometheusMetricsModel,
  )

  lazy val skunk = Seq(
    "org.tpolecat" %% "skunk-core" % Versions.skunk,
  )

  lazy val sourcepos = Seq(
    "org.tpolecat" %% "sourcepos" % Versions.sourcepos,
  )

  lazy val tapir = Seq(
    "com.softwaremill.sttp.tapir"           %% "tapir-core"               % Versions.tapir,
    "com.softwaremill.sttp.tapir"           %% "tapir-http4s-server"      % Versions.tapir,
    "com.softwaremill.sttp.tapir"           %% "tapir-swagger-ui-bundle"  % Versions.tapir,
    "com.softwaremill.sttp.tapir"           %% "tapir-jsoniter-scala"     % Versions.tapir,
    "com.softwaremill.sttp.tapir"           %% "tapir-json-circe"         % Versions.tapir,
    "com.softwaremill.sttp.tapir"           %% "tapir-prometheus-metrics" % Versions.tapir,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros"    % "2.30.15",
  )

  lazy val testContainers = Seq(
    "com.dimafeng" %% "testcontainers-scala"            % Versions.testContainers,
    "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.testContainers,
    postgresql,
  )

  lazy val twiddles = Seq(
    "org.typelevel" %% "twiddles-core" % Versions.twiddles,
  )

  lazy val weaver = Seq(
    "com.disneystreaming" %% "weaver-cats" % Versions.weaver,
  )
}
