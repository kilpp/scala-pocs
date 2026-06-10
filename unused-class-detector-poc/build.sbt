ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.4"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "unused-class-detector-poc",
    libraryDependencies ++= Seq(
      "org.scalameta" %% "scalameta" % "4.17.0",
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.UnusedClassDetectorPoc")
  )
