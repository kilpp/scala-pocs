ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / organization := "com.example"

val springVersion         = "6.2.8"
val springSecurityVersion = "6.5.1"
val jakartaServletVersion  = "6.1.0"
val tomcatVersion         = "11.0.22"
val slf4jVersion          = "2.0.17"
val scalaTestVersion      = "3.2.20"

lazy val root = (project in file("."))
  .settings(
    name := "spring-security-poc",
    libraryDependencies ++= Seq(
      "org.springframework"         %  "spring-webmvc"           % springVersion,
      "org.springframework.security" % "spring-security-web"     % springSecurityVersion,
      "org.springframework.security" % "spring-security-config"  % springSecurityVersion,
      "org.apache.tomcat.embed"     %  "tomcat-embed-core"       % tomcatVersion,
      "jakarta.servlet"             %  "jakarta.servlet-api"     % jakartaServletVersion,
      "org.slf4j"                   %  "slf4j-simple"            % slf4jVersion,
      "org.springframework"          % "spring-test"             % springVersion         % Test,
      "org.springframework.security" % "spring-security-test"    % springSecurityVersion % Test,
      "org.scalatest"               %% "scalatest"               % scalaTestVersion      % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    ),
    Compile / mainClass := Some("com.example.SpringSecurityPoc"),
    run / fork          := true,
    Test / fork         := true
  )
