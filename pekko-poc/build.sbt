ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val pekkoVersion     = "1.6.0"
val pekkoHttpVersion = "1.3.0"
val logbackVersion   = "1.5.18"

lazy val root = (project in file("."))
  .settings(
    name := "pekko-poc",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed"            % pekkoVersion,
      "org.apache.pekko" %% "pekko-stream"                 % pekkoVersion,
      "org.apache.pekko" %% "pekko-cluster-typed"          % pekkoVersion,
      "org.apache.pekko" %% "pekko-cluster-sharding-typed" % pekkoVersion,
      "org.apache.pekko" %% "pekko-persistence-typed"      % pekkoVersion,
      "org.apache.pekko" %% "pekko-persistence-testkit"    % pekkoVersion,
      "org.apache.pekko" %% "pekko-distributed-data"       % pekkoVersion,
      "org.apache.pekko" %% "pekko-serialization-jackson"  % pekkoVersion,
      "org.apache.pekko" %% "pekko-http"                   % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-http-spray-json"        % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-actor-testkit-typed"    % pekkoVersion     % Test,
      "org.apache.pekko" %% "pekko-stream-testkit"         % pekkoVersion     % Test,
      "ch.qos.logback"   %  "logback-classic"              % logbackVersion,
      "org.scalatest"    %% "scalatest"                    % "3.2.20"         % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.Main"),
    run / fork          := true,
    run / connectInput  := true,
    Compile / run / javaOptions ++= Seq(
      "-Dpekko.coordinated-shutdown.exit-jvm=on"
    )
  )
