scalaVersion := "2.13.3"

name := "simple-parser"
version := "0.1"

//libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

lazy val root = (project in file(".")).
  settings(
    name := "simple-parser-cli"
 )
