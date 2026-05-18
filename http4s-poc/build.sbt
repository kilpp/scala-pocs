ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val http4sVersion         = "0.23.32"
val circeVersion          = "0.14.15"
val munitCatsEffectVersion = "2.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "http4s-poc",
    libraryDependencies ++= Seq(
      "org.http4s"    %% "http4s-ember-server" % http4sVersion,
      "org.http4s"    %% "http4s-ember-client" % http4sVersion,
      "org.http4s"    %% "http4s-dsl"          % http4sVersion,
      "org.http4s"    %% "http4s-circe"        % http4sVersion,
      "io.circe"      %% "circe-core"          % circeVersion,
      "io.circe"      %% "circe-generic"       % circeVersion,
      "org.typelevel" %% "munit-cats-effect"   % munitCatsEffectVersion % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.Http4sPoc")
  )
