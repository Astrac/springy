name := "springy"

organization := "astrac"

scalacOptions := Seq("-feature", "-deprecation")

scalaVersion := "2.11.8"

val circeV = "0.3.0"

libraryDependencies ++= Seq(
  "org.elasticsearch" % "elasticsearch" % "2.3.0",
  "io.circe" %% "circe-core" % circeV,
  "io.circe" %% "circe-generic" % circeV,
  "io.circe" %% "circe-parser" % circeV,
  "org.typelevel" %% "cats" % "0.4.1",
  "org.scalatest" %% "scalatest"     % "2.2.1",
  "org.apache.commons" % "commons-io" % "1.3.2"
)

enablePlugins(GitVersioning)

git.useGitDescribe := true
