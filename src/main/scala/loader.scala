package xxx.desu.bzzt

import akka.actor.ActorSystem
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory

class BzzTLoader extends Bootable {

//   val cfg = ConfigFactory.load getConfig "netserve"

  val systema =
    Seq ( "BzzTLoader"
//         ) map (ActorSystem (_, cfg))
        ) map (ActorSystem (_))

  def startup {
    systema foreach (ServerStarter (_))
  }

  def shutdown {
    systema foreach (_ shutdown)
  }
}

