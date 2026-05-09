ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "json4s-poc",
    libraryDependencies ++= Seq(
      "org.json4s"    %% "json4s-native"  % "4.0.7",
      "org.json4s"    %% "json4s-jackson" % "4.0.7",
      "org.json4s"    %% "json4s-ext"     % "4.0.7",
      "org.scalatest" %% "scalatest"      % "3.2.20" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.Json4sPoc")
  )
