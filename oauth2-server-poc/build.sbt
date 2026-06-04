ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val http4sVersion    = "0.23.32"
val circeVersion     = "0.14.15"
val jwtScalaVersion  = "11.0.4"
val slf4jVersion     = "2.0.17"
val scalaTestVersion = "3.2.20"

lazy val root = (project in file("."))
  .settings(
    name := "oauth2-server-poc",
    libraryDependencies ++= Seq(
      "org.http4s"        %% "http4s-ember-server" % http4sVersion,
      "org.http4s"        %% "http4s-dsl"          % http4sVersion,
      "org.http4s"        %% "http4s-circe"        % http4sVersion,
      "io.circe"          %% "circe-core"          % circeVersion,
      "io.circe"          %% "circe-parser"        % circeVersion,
      "com.github.jwt-scala" %% "jwt-circe"        % jwtScalaVersion,
      "org.slf4j"         %  "slf4j-simple"        % slf4jVersion,
      "org.http4s"        %% "http4s-ember-client" % http4sVersion    % Test,
      "org.scalatest"     %% "scalatest"           % scalaTestVersion % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.OAuth2ServerPoc"),
    run / fork          := true,
    Test / fork         := true
  )
