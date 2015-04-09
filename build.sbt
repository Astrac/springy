lazy val springy = project.in(file(".")).settings(

  name := "springy",

  scalacOptions := Seq("-feature", "-deprecation"),

  resolvers ++= Seq(
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"
  ),

  scalaVersion := "2.11.6",

  libraryDependencies ++= Seq(
    "org.elasticsearch" % "elasticsearch" % "1.5.0",
    "io.spray" %% "spray-json" % "1.3.1",
    "org.scalaz" %% "scalaz-core" % "7.1.1",
    "org.scalaz" %% "scalaz-effect" % "7.1.1",
    "org.scalatest" %% "scalatest"     % "2.2.1" % Test,
    "org.apache.commons" % "commons-io" % "1.3.2" % Test
  )
)

enablePlugins(GitVersioning)
