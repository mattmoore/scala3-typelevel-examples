import sbtwelcome.*

ThisBuild / scalaVersion                        := "3.5.0"
ThisBuild / Test / parallelExecution            := false
ThisBuild / githubWorkflowJavaVersions          := Seq(JavaSpec.temurin("22"))
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

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
    `geolocation-it`,
  )

lazy val `cats-examples` = (project in file("cats-examples"))
  .settings(
    name := "cats-examples",
    libraryDependencies ++= Dependencies.Projects.typelevelExamples,
  )

lazy val `fs2-examples` = (project in file("fs2-examples"))
  .settings(
    name := "fs2-examples",
    libraryDependencies ++= Dependencies.Projects.fs2Examples,
  )

lazy val `skunk-examples` = (project in file("skunk-examples"))
  .settings(
    name := "skunk-examples",
    libraryDependencies ++= Dependencies.Projects.skunkExamples,
    fork := true,
  )

lazy val geolocation = (project in file("geolocation"))
  .enablePlugins(
    JavaAgent,
    JavaAppPackaging,
    DockerPlugin,
  )
  .settings(
    name := "geolocation",
    libraryDependencies ++= Dependencies.Projects.geolocation,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    fork := true,
  )
  .settings(
    Docker / packageName := "geolocation",
    Docker / version     := "latest",
    dockerExposedPorts ++= Seq(8080),
    dockerBaseImage := "openjdk:22",
  )
  .settings(
    javaOptions += "-Dotel.java.global-autoconfigure.enabled=true",
    javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "1.24.0",
    javaOptions ++= Seq(
      "-Dotel.java.global-autoconfigure.enabled=true",
      s"-Dotel.service.name=${name.value}",
    ),
  )

lazy val `geolocation-it` = (project in file("geolocation-it"))
  .dependsOn(geolocation % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Dependencies.Projects.geolocationIt,
    fork := true,
  )

addCommandAlias("geoTest", "geolocation/test; geolocation-it/test")
addCommandAlias("formatAll", "scalafmtAll; scalafmtSbt")

lazy val welcomeSettings = Seq(
  logo      := Embroidery.projectLogo,
  logoColor := scala.Console.RED,
  usefulTasks := Seq(
    UsefulTask("formatAll", "Format all Scala code.").alias("f"),
    UsefulTask("geoTest", "Run geolocation unit and integration tests.").alias("geo-test"),
    UsefulTask("geolocation/run", "Run geolocation example.").alias("geo"),
    UsefulTask("cats-examples/test", "Run cats examples.").alias("cats"),
    UsefulTask("fs2-examples/test", "Run fs2 examples.").alias("fs2"),
    UsefulTask("skunk-examples/run", "Run skunk standalone examples.").alias("skunk"),
  ),
)
