ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "zio-poc",
    libraryDependencies ++= Seq(
      // ZIO core + streams
      "dev.zio" %% "zio"         % "2.1.26",
      "dev.zio" %% "zio-streams" % "2.1.26",
      // HTTP
      "dev.zio" %% "zio-http"    % "3.11.1",
      // JSON
      "dev.zio" %% "zio-json"    % "0.9.2",
      // Config
      "dev.zio" %% "zio-config"            % "4.0.7",
      "dev.zio" %% "zio-config-typesafe"   % "4.0.7",
      "dev.zio" %% "zio-config-magnolia"   % "4.0.7",
      // Logging
      "dev.zio" %% "zio-logging"       % "2.5.3",
      "dev.zio" %% "zio-logging-slf4j2" % "2.5.3",
      "ch.qos.logback" % "logback-classic" % "1.5.18",
      // Schema (serialisation / validation)
      "dev.zio" %% "zio-schema"            % "1.8.5",
      "dev.zio" %% "zio-schema-derivation" % "1.8.5",
      "dev.zio" %% "zio-schema-json"       % "1.8.5",
      // Database: H2 + raw JDBC wrapped in ZIO
      "com.h2database" % "h2" % "2.3.232",
      // Test
      "dev.zio"       %% "zio-test"          % "2.1.26" % Test,
      "dev.zio"       %% "zio-test-sbt"      % "2.1.26" % Test,
      "dev.zio"       %% "zio-http-testkit"  % "3.11.1" % Test,
      "org.scalatest" %% "scalatest"         % "3.2.20" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.ZioPoc")
  )
