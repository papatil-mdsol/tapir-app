ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"
ThisBuild / organization := "com.pankaj"

val tapirVersion = "1.5.1"
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
  .settings(libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion)
  .settings(libraryDependencies ++= Seq(
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml" % "0.4.0",
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs" % tapirVersion
  ))
libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.16"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % "test"

sonarUseExternalConfig := true
scapegoatVersion in ThisBuild := "1.3.9"
scapegoatReports := Seq("xml")

scalacOptions in Scapegoat += "-P:scapegoat:overrideLevels:all=Warning"
