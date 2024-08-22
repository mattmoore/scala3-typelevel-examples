import sbtwelcome.*

ThisBuild / scalaVersion             := "3.4.2"
ThisBuild / Test / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "scala3-typelevel-examples",
    welcomeSettings
  )
  .aggregate(
    `cats-examples`
  )

lazy val `cats-examples` = (project in file("cats-examples"))
  .settings(
    name := "cats-examples",
    libraryDependencies ++= Dependencies.queue
  )

lazy val welcomeSettings = Seq(
  logo      := Embroidery.projectLogo,
  logoColor := scala.Console.BLUE,
  usefulTasks := Seq(
    UsefulTask("test", "Run all unit tests.").alias("ut")
  )
)
