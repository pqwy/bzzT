package xxx.desu.bzzt

import akka.actor.{ ActorSystem, Actor, Props }
import akka.kernel.Bootable

/** Runner class for the Akka microkernel.
 */
class BzzTLoader extends Bootable {

  lazy val systema =
    Seq ( "BzzTLoader"
        ) map (ActorSystem (_))

  def startup {
    systema foreach (ServerStarter (_))
  }

  def shutdown {
    systema foreach (_ shutdown)
  }
}

/** Standalone runner
 */
object BzzTLoaderApp extends App { new BzzTLoader startup }

/** Runner that ties its lifecycle to a supervising
 * actor, for running recursively under itself.
 */
class BzzTLoaderActor {

  lazy val loader = new BzzTLoader

  def apply = {
    loader.startup
    loader.systema foreach (system =>
        system.actorOf ( Props (new Guardian (system)), "guardian" )
    )
  }

  class Guardian (system : ActorSystem) extends Actor {
    def receive = Map ()
    override def postStop = system.shutdown
  }
}

