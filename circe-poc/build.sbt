ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val circeVersion = "0.14.15"

lazy val root = (project in file("."))
  .settings(
    name := "circe-poc",
    libraryDependencies ++= Seq(
      "io.circe"      %% "circe-core"    % circeVersion,
      "io.circe"      %% "circe-generic" % circeVersion,
      "io.circe"      %% "circe-parser"  % circeVersion,
      "org.scalatest" %% "scalatest"     % "3.2.20" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.CircePoc")
  )
