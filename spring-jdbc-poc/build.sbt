ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val springBootVersion = "4.0.6"
val h2Version         = "2.4.240"
val scalaTestVersion  = "3.2.20"

lazy val root = (project in file("."))
  .settings(
    name := "spring-jdbc-poc",
    libraryDependencies ++= Seq(
      "org.springframework.boot" %  "spring-boot-starter-data-jdbc" % springBootVersion,
      "com.h2database"           %  "h2"                            % h2Version,
      "org.springframework.boot" %  "spring-boot-starter-test"      % springBootVersion % Test,
      "org.scalatest"            %% "scalatest"                     % scalaTestVersion  % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.SpringJdbcPocApp"),
    run / fork          := true,
    Test / fork         := true
  )
