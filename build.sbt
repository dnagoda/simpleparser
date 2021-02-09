scalaVersion := "2.13.3"

name := "simple-parser"
version := "0.1"

val circeVersion = "0.13.0"
val http4sVersion = "0.21.15"
val slf4jVersion = "1.7.9"

lazy val commonSettings = Seq(
  target := { baseDirectory.value / "target" },
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.3.1",
    "org.scalactic" %% "scalactic" % "3.2.2",
    "org.scalatest" %% "scalatest" % "3.2.2" % "test",
    "org.scalatest" %% "scalatest-flatspec" % "3.2.2" % "test"
  )
)

lazy val core = (project in file("core")).
  settings(
    name := "simple-parser-core",
    commonSettings
 )

lazy val cli = (project in file("cli")).
  settings(
    name := "simple-parser-cli",
    commonSettings
 ).dependsOn(core % ("compile->compile;test->test"))

lazy val api = ((project in file("api")).
  settings(
    name := "simple-parser-api",
    commonSettings,
    scalacOptions += "-Ypartial-unification",
    libraryDependencies += "io.circe" %% "circe-core" % circeVersion,
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-circe"
    ).map(_ % http4sVersion),
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api",
      "org.slf4j" % "slf4j-simple"
    ).map(_ % slf4jVersion)
  ).dependsOn(core % ("compile->compile;test->test"))
)