package xxx.desu.bzzt

import akka.actor.ActorSystem
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory

class BzzLoader extends Bootable {

  val cfg = ConfigFactory.load getConfig "netserve"

  val systema =
    Seq ( "BzzLoader"
        ) map (ActorSystem (_, cfg))

  def startup {
    systema foreach (ServerStarter (_))
  }

  def shutdown {
    systema foreach (_ shutdown)
  }
}
