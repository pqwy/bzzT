
name         := "bzzt-client-packaged"

description  := "create jar to be sent to bzzt-server for testing and demo"

version      := "0.0.1"

organization := "hr.element"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq (
    "com.typesafe.akka" % "akka-actor"  % "2.0-M3"
  )
