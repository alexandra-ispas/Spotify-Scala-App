ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "Spotify"
  )

libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.29"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "test"

libraryDependencies += "com.typesafe" % "config" % "1.4.2"