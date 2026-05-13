ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "cats-poc",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % "2.13.0",
      "org.typelevel" %% "cats-free"   % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.7.0",
      "org.scalatest" %% "scalatest"   % "3.2.20" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.CatsPoc")
  )
