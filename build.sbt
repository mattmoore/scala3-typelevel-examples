import sbtwelcome.*

ThisBuild / scalaVersion             := "3.4.2"
ThisBuild / Test / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "scala3-typelevel-examples",
    welcomeSettings
  )
  .aggregate(
    `cats-examples`,
    `skunk-examples`,
    `http-server`
  )

lazy val `cats-examples` = (project in file("cats-examples"))
  .settings(
    name := "cats-examples",
    libraryDependencies ++= Dependencies.typelevelExamples
  )

lazy val `skunk-examples` = (project in file("skunk-examples"))
  .settings(
    name := "skunk-examples",
    libraryDependencies ++= Dependencies.postgres,
    fork := true
  )

lazy val `http-server` = (project in file("http-server"))
  .settings(
    name := "http-server",
    libraryDependencies ++= Dependencies.httpServer,
    fork := true
  )

lazy val welcomeSettings = Seq(
  logo      := Embroidery.projectLogo,
  logoColor := scala.Console.RED,
  usefulTasks := Seq(
    UsefulTask("test", "Run all unit tests.").alias("ut")
  )
)
