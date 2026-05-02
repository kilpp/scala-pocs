ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "gson-poc",
    libraryDependencies ++= Seq(
      "com.google.code.gson" %  "gson"      % "2.14.0",
      "org.scalatest"        %% "scalatest" % "3.2.20" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.GsonPoc")
  )
