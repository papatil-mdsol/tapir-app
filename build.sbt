ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"
ThisBuild / organization := "com.pankaj"

val tapirVersion = "1.4.0"
val http4sVersion = "0.23.19"


lazy val endpoints = (project in file("endpoints"))
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion)

lazy val client = (project in file("client"))
  .dependsOn(endpoints)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % tapirVersion)

lazy val server = (project in file("server"))
  .dependsOn(endpoints)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-akka-http-server" % tapirVersion)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion)
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.5.1")
  .settings(libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % "1.2.10",
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % "0.20.2",
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s" % "0.18.3",
    "com.softwaremill.sttp.tapir" %% "tapir-redoc-http4s" % "0.18.3",
    "com.softwaremill.sttp.shared" %% "http4s" % http4sVersion
  ))