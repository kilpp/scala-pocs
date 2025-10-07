val scala3Version = "3.7.3"

lazy val root = (project in file("."))
  .settings(
    name := "bitonic-scala3",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.5.1",
      "dev.zio" %% "zio-redis" % "1.1.5",
      "dev.zio" %% "zio-schema-protobuf" % "1.7.5",
      "org.scalameta" %% "munit" % "1.2.0" % Test
    )
  )
