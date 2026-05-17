ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "scalaz-poc",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core"   % "7.3.8",
      "org.scalaz" %% "scalaz-effect" % "7.3.8",
      "org.scalatest" %% "scalatest"  % "3.2.20" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.ScalazPoc")
  )
