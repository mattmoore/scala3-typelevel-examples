import sbtwelcome.*

ThisBuild / scalaVersion                        := "3.5.0"
ThisBuild / Test / parallelExecution            := false
ThisBuild / githubWorkflowJavaVersions          := Seq(JavaSpec.temurin("22"))
ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / scalacOptions ++= Seq("-no-indent")

lazy val welcomeSettings = Seq(
  logo      := Embroidery.projectLogo,
  logoColor := scala.Console.RED,
  usefulTasks := Seq(
    UsefulTask("welcome", "Show this welcome screen.").alias("w"),
    UsefulTask("reload", "Reload sbt.").alias("r"),
    UsefulTask("formatAll", "Format all Scala code.").alias("f"),
    UsefulTask("cats-examples/test", "Run cats examples.").alias("cats"),
    UsefulTask("fs2-examples/test", "Run fs2 examples.").alias("fs2"),
    UsefulTask("skunk-examples/run", "Run skunk standalone examples.").alias("skunk"),
    UsefulTask("test", "Run all unit tests.").alias("t"),
  ),
)

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
    `fs2-examples`,
    `graph-examples`,
  )

lazy val `cats-examples` = (project in file("cats-examples"))
  .settings(
    name := "cats-examples",
    libraryDependencies ++= Dependencies.Projects.typelevelExamples,
    fork := true,
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

lazy val `parser-combinators` = (project in file("parser-combinators"))
  .settings(
    name := "parser-combinators",
    libraryDependencies ++= Dependencies.Projects.parserCombinators,
  )

lazy val `recursion-schemes` = (project in file("recursion-schemes"))
  .settings(
    name := "recursion-schemes",
    libraryDependencies ++= Dependencies.Projects.typelevelExamples,
    fork := true,
  )

lazy val `graph-examples` = (project in file("graph-examples"))
  .settings(
    name := "graph-examples",
    libraryDependencies ++= Dependencies.Projects.typelevelExamples,
    fork := true,
  )

addCommandAlias("unitTests", "coverageOn; cats-examples/test; cats-examples/coverageReport; coverageOff")
addCommandAlias("formatAll", "scalafmtAll; scalafmtSbt")
