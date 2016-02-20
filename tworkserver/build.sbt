name := """tworkserver"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,PlayEbean)

scalaVersion := "2.11.6"

resolvers += "release repository" at "http://chanan.github.io/maven-repo/releases/"

resolvers += "snapshot repository" at "http://chanan.github.io/maven-repo/snapshots/"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,

  "org.postgresql" % "postgresql" % "9.4.1207.jre7",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.9.Final",
  "org.json"%"org.json"%"chargebee-1.0",
  "commons-io" % "commons-io" % "2.4"
)


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

fork in run := false


fork in run := true