import sbtwelcome.*

ThisBuild / scalaVersion             := "3.4.2"
ThisBuild / Test / parallelExecution := false

lazy val root = (project in file("."))
  .enablePlugins(
    GitBranchPrompt,
    GitVersioning,
  )
  .settings(
    name := "scala3-typelevel-examples",
    welcomeSettings,
  )
  .aggregate(
    `cats-examples`,
    `skunk-examples`,
    geolocation,
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

lazy val geolocation = (project in file("geolocation"))
  .enablePlugins(
    JavaAppPackaging,
    DockerPlugin,
  )
  .settings(
    name := "geolocation",
    libraryDependencies ++= Dependencies.Projects.geolocation,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    fork := true,
    // Docker
    Docker / packageName := "geolocation",
    Docker / version := "latest",
    dockerExposedPorts ++= Seq(8080),
    dockerBaseImage := "openjdk:22",
  )

lazy val `geolocation-it` = (project in file("geolocation-it"))
  .dependsOn(geolocation)
  .settings(
    libraryDependencies ++= Dependencies.Projects.geolocationIt,
    fork := true,
  )

addCommandAlias("formatAll", "scalafmtAll; scalafmtSbt")

lazy val welcomeSettings = Seq(
  logo      := Embroidery.projectLogo,
  logoColor := scala.Console.RED,
  usefulTasks := Seq(
    UsefulTask("formatAll", "Format all Scala code.").alias("f"),
    UsefulTask("geolocation/run", "Run geolocation example.").alias("geo"),
    UsefulTask("cats-examples/test", "Run cats standalone examples.").alias("cats"),
    UsefulTask("skunk-examples/run", "Run skunk standalone examples.").alias("skunk"),
  ),
)
