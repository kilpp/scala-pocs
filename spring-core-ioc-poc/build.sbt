ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val springVersion        = "6.2.8"
val jakartaAnnotationVer = "3.0.0"
val slf4jVersion         = "2.0.17"
val scalaTestVersion     = "3.2.20"

lazy val root = (project in file("."))
  .settings(
    name := "spring-core-ioc-poc",
    libraryDependencies ++= Seq(
      "org.springframework" %  "spring-context"         % springVersion,
      "jakarta.annotation"  %  "jakarta.annotation-api" % jakartaAnnotationVer,
      "org.slf4j"           %  "slf4j-simple"           % slf4jVersion,
      "org.scalatest"       %% "scalatest"              % scalaTestVersion % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.SpringCoreIocPoc"),
    run / fork          := true,
    Test / fork         := true
  )
