import sbt.*

object Dependencies {
  lazy val munit = Seq("org.scalameta" %% "munit" % Versions.munit % Test)

  lazy val cats = Seq(
    "org.typelevel" %% "cats-core"   % Versions.catsCore,
    "org.typelevel" %% "cats-effect" % Versions.catsEffect
  )

  lazy val http4s = Seq(
    "org.http4s" %% "http4s-ember-client" % Versions.http4s,
    "org.http4s" %% "http4s-ember-server" % Versions.http4s,
    "org.http4s" %% "http4s-dsl"          % Versions.http4s
  )

  lazy val skunk = Seq(
    "org.tpolecat" %% "skunk-core" % Versions.skunk
  )

  lazy val typelevelExamples =
    cats ++
      munit

  lazy val postgres =
    cats ++
      skunk

  lazy val httpServer =
    cats ++
      http4s ++
      skunk ++
      munit
}
