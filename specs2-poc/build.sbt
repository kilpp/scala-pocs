ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val specs2Version = "5.9.0"

lazy val root = (project in file("."))
  .settings(
    name := "specs2-poc",
    libraryDependencies ++= Seq(
      "org.specs2" %% "specs2-core"       % specs2Version % Test,
      "org.specs2" %% "specs2-matcher"    % specs2Version % Test,
      "org.specs2" %% "specs2-junit"      % specs2Version % Test,
      "org.specs2" %% "specs2-scalacheck" % specs2Version % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Test / scalacOptions += "-Yretain-trees",
    Compile / mainClass := Some("com.example.Specs2Poc")
  )
