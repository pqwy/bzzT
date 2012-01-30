import sbt._
import Keys._


object BzzTBuild extends Build {

  lazy val everything = Project (
      id   = "everything"
    , base = file (".")
    ) aggregate ( server
                , hostedcode
                , clientinstrumentation )

  lazy val server = Project (
      id   = "bzzt-server"
    , base = file ("bzzt-server")
    ) dependsOn (hostedcode % "test->compile")

  lazy val hostedcode = Project (
      id   = "bzzt-example-hosted-code"
    , base = file ("bzzt-example-hosted-code") )

  lazy val clientinstrumentation = Project (
      id   = "bzzt-client-instrumentation"
    , base = file ("bzzt-client-instrumentation") )
}
