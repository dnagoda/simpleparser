scalaVersion := "2.13.3"

name := "simple-parser"
version := "0.1"

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
