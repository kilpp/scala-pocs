ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val tapirVersion  = "1.13.19"
val http4sVersion = "0.23.32"
val sttpVersion   = "4.0.13"

lazy val root = (project in file("."))
  .settings(
    name := "tapir-poc",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core"               % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle"  % tapirVersion,
      "org.http4s"                  %% "http4s-ember-server"      % http4sVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server"   % tapirVersion       % Test,
      "com.softwaremill.sttp.client4" %% "core"                   % sttpVersion         % Test,
      "com.softwaremill.sttp.client4" %% "cats"                   % sttpVersion         % Test,
      "org.scalatest"               %% "scalatest"                % "3.2.20"            % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.TapirPoc")
  )
