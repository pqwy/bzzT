package xxx.desu.bzzt
package tests

import org.scalatest.FeatureSpec
import org.scalatest.matchers.MustMatchers

import akka.actor._

import akka.pattern.ask
import akka.dispatch.{ Await, Future }

import akka.util.Timeout
import akka.util.duration._

import com.typesafe.config.ConfigFactory

import java.lang.reflect.InvocationTargetException
import java.lang.Thread.sleep


protected [tests]
class AkkaTests extends FeatureSpec with MustMatchers {

  def withActorSystem [A] (name : String, cfgRoot : String)
                          (f : ActorSystem => A) : A = {

    val cfg = ConfigFactory.load getConfig cfgRoot
    val system = ActorSystem (name, cfg)
    try Function.const (f (system)) (sleep (50))
    finally { system.shutdown ; sleep (50) }
  }

  def withServer [A] (cfgRoot : String) (f : Map[String, ActorRef] => A)
    = withActorSystem ("BzzTServer", cfgRoot) ((ServerStarter (_)) andThen f)

  def withClient [A] (cfgRoot : String) (f : ActorSystem => A)
    = withActorSystem ("BzzTClient", cfgRoot) (f)

  implicit val defaultTimeout = Timeout (10 seconds)


  def basicConnect (title : String)
                   (cfgClient : String, cfgServer : String)
                   (locateServer : (Map[String, ActorRef], ActorSystem) => ActorRef) {

    feature (title) {

      scenario ("booting") {
        withClient (cfgClient) (_ => Unit)
        withServer (cfgServer) (_ => Unit)
      }

      def rt (msg : Any) =
        withServer (cfgServer) ( server =>
          withClient (cfgClient) ( csystem =>
            Await result ( locateServer (server, csystem) ? msg, 10 seconds )
        ) )

      scenario ("round-trip / explicit method") {
        rt ( ("desu.B", "smile", scalaJar) ) must be ("happy")
      }
      scenario ("round-trip / apply") {
        rt ( ("desu.B", scalaJar) ) must be (Map ("hell" -> "yeah!"))
      }
      scenario ("round-trip / manifest") (pending)
      scenario ("inner exceptions") {
        rt ( ("desu.C", scalaJar) ) match {
          case _ : InvocationTargetException =>
        }
      }
      scenario ("outer exceptions") {
        intercept[ClassNotFoundException] ( rt ( ("no.no.no.no.no", scalaJar) ) )
      }
    }

  }

  ( basicConnect ("over in-process akka")
                 ("inproc", "inproc")
                 ((server, _) => server ("run")) )

  ( basicConnect ("over loopback akka")
                 ("localnetclient", "localnetserve")
                 ((_, csystem) =>
                     csystem actorFor (
                       "akka://BzzTServer@localhost:3001/user/run")
                 )
  )

}
