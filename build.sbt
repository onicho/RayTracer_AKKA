name := "RayTracer_AKKA"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.4.1"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0-M15" % "test"
libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % "2.4.2" % "test"