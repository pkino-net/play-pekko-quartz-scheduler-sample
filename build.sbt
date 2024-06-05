name := """play-pekko-quartz-scheduler-sample"""
organization := "com.pkinop"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
libraryDependencies += "io.github.samueleresca" %% "pekko-quartz-scheduler" % "1.0.0-pekko-1.0.x"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.pkinop.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.pkinop.binders._"
