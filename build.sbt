
name         := "bzzloader"

version      := "0.0.1"

organization := "hr.element"

scalaVersion := "2.9.1"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq (
    "org.scalaz" %% "scalaz-core" % "6.0.3"
  , "com.typesafe.akka" % "akka-actor"  % "2.0-M2"
  , "com.typesafe.akka" % "akka-remote" % "2.0-M2"
  , "com.typesafe.akka" % "akka-kernel" % "2.0-M2"
  )
