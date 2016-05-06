name := "springy"

organization := "astrac"

scalacOptions := Seq("-feature", "-deprecation")

scalaVersion := "2.11.8"

val circeV = "0.4.1"

libraryDependencies ++= Seq(
  "org.elasticsearch" % "elasticsearch" % "2.3.2",
  "io.circe" %% "circe-core" % circeV,
  "io.circe" %% "circe-generic" % circeV,
  "io.circe" %% "circe-parser" % circeV,
  "org.typelevel" %% "cats" % "0.5.0",
  "org.scalatest" %% "scalatest"     % "2.2.6"
)

enablePlugins(GitVersioning)

git.useGitDescribe := true
