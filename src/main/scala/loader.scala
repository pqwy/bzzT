package xxx.desu.bzzt

import akka.actor.ActorSystem
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory

class BzzTLoader extends Bootable {

  val cfg = ConfigFactory.load getConfig "netserve"

  val systema =
    Seq ( "BzzTLoader"
        ) map (ActorSystem (_, cfg))

  def startup {
    systema foreach (ServerStarter (_))
  }

  def shutdown {
    systema foreach (_ shutdown)
  }
}

object BzzTLoaderApp extends App {
  new BzzTLoader startup ;
  while (true)
    java.lang.Thread sleep Integer.MAX_VALUE
}
