package xxx.desu.bzzt

import akka.actor._
import akka.dispatch.{ Await, Dispatchers }
import akka.util.duration._

import java.util.concurrent.atomic.AtomicReference


object ServerStarter {
  def apply (system: ActorSystem) = {
    system actorOf (RunServ ( isolating = false ), name = "run")
  }
}

object RunServ {

  import SupervisorStrategy._

  def apply (isolating : Boolean = false) =
    Props ( new RunServ ( new Loaders (isolating) ) )
}

class RunServ (newLoader : Loaders) extends Actor {

  val state = Core newDefaultState

  val runner = (
    Props [Runner]
    withDispatcher "akka.actor.pinning-dispatcher"
  )

  def receive = {
    case cmd @ (k : String, b : Array[Byte]) =>
      context actorOf (runner) forward RunThis (newLoader, state, k, b)
  }

  import SupervisorStrategy.{ Stop, Escalate }

  override val supervisorStrategy =
    OneForOneStrategy () {
      case _ : Exception => Stop
      case _             => Escalate
    }
}

class Runner extends Actor {

  import Status.Failure

  def receive = {

    case cmd: RunThis =>

      lazy val run = Core run cmd fold (identity, identity)
      sender ! (try run catch { case t: Throwable => Failure (t) })

      context stop self
  }

}

