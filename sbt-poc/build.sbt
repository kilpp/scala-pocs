ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.4"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "sbt-poc",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.run")
  )

// Custom task: `sbt hello` prints a message — shows how tasks are defined.
lazy val hello = taskKey[Unit]("Prints a friendly greeting from sbt.")
hello := {
  val log = streams.value.log
  log.info(s"Hello from ${name.value} ${version.value} on Scala ${scalaVersion.value}")
}
