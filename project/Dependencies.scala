import sbt.*

object Dependencies {
  lazy val catsCore   = Seq("org.typelevel" %% "cats-core" % Versions.catsCore)
  lazy val catsEffect = Seq("org.typelevel" %% "cats-effect" % Versions.catsEffect)
  lazy val munit      = Seq("org.scalameta" %% "munit" % Versions.munit % Test)
  lazy val queue      = catsCore ++ catsEffect ++ munit
}
