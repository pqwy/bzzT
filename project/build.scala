import sbt._
import Keys._


object BzzTBuild extends Build {

  lazy val everything = Project (
      id   = "everything"
    , base = file (".")
    ) aggregate ( server
                , packagedClient
                , clientInstrumentation )

  lazy val server = Project (
      id   = "bzzt-server"
    , base = file ("server")
    ) dependsOn (packagedClient % "test->compile")

  lazy val packagedClient = Project (
      id   = "client-packaged"
    , base = file ("test-client-packaged") )

  lazy val clientInstrumentation = Project (
      id   = "client-instrumentation"
    , base = file ("client-instrumentation") )
}
