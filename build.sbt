scalaVersion := "2.13.3"

name := "simple-parser"
version := "0.1"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-flatspec" % "3.2.2" % "test"

lazy val root = (project in file(".")).
  settings(
    name := "simple-parser-cli"
 )
