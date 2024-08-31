import sbtwelcome.*

ThisBuild / scalaVersion             := "3.4.2"
ThisBuild / Test / parallelExecution := false

lazy val root = (project in file("."))
  .settings(
    name := "scala3-typelevel-examples",
    welcomeSettings,
  )
  .aggregate(
    `cats-examples`,
    `skunk-examples`,
    `http-server`,
  )

lazy val `cats-examples` = (project in file("cats-examples"))
  .settings(
    name := "cats-examples",
    libraryDependencies ++= Dependencies.Projects.typelevelExamples,
  )

lazy val `skunk-examples` = (project in file("skunk-examples"))
  .settings(
    name := "skunk-examples",
    libraryDependencies ++= Dependencies.Projects.skunkExamples,
    fork := true,
  )

lazy val `http-server` = (project in file("http-server"))
  .settings(
    name := "http-server",
    libraryDependencies ++= Dependencies.Projects.httpServer,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    fork := true,
  )

lazy val `http-server-it` = (project in file("http-server-it"))
  .dependsOn(`http-server`)
  .settings(
    libraryDependencies ++= Dependencies.Projects.httpServerIt,
  )

addCommandAlias("formatAll", "scalafmtAll; scalafmtSbt")

lazy val welcomeSettings = Seq(
  logo      := Embroidery.projectLogo,
  logoColor := scala.Console.RED,
  usefulTasks := Seq(
    UsefulTask("formatAll", "Format all Scala code.").alias("f"),
    UsefulTask("http-server/run", "Run http server example.").alias("http"),
    UsefulTask("cats-examples/test", "Run cats standalone examples.").alias("cats"),
    UsefulTask("skunk-examples/run", "Run skunk standalone examples.").alias("skunk"),
  ),
)
