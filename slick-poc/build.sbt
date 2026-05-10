ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "slick-poc",
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick"           % "3.6.1",
      "com.h2database"     %  "h2"              % "2.4.240",
      "ch.qos.logback"     %  "logback-classic" % "1.5.32",
      "org.scalatest"      %% "scalatest"       % "3.2.20" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.SlickPoc")
  )
