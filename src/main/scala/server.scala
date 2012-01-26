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

  def apply (isolating : Boolean = false) =
    Props ( new RunServ ( new Loaders (isolating) ) )
}

class RunServ (newLoader : Loaders) extends Actor {

  val state = Core newDefaultState

  val runner = (
    Props [Runner]
    withDispatcher "akka.actor.pinning-dispatcher"
  )

  import SupervisorStrategy.{ Stop, Escalate }

  override val supervisorStrategy =
    OneForOneStrategy () {
      case _ : Exception => Stop
      case _             => Escalate
    }

  def receive = decode andThen ( context actorOf (runner) forward _ )

  def decode : PartialFunction [Any, RunThis] = {

    case (cls : String, blob : Array[Byte]) =>
      RunThis ( newLoader, state, EnterCls (cls), blob )

    case (cls : String, meth : String, blob : Array[Byte]) =>
      RunThis ( newLoader, state, EnterClsMeth (cls, meth), blob )

    case blob : Array[Byte] =>
      RunThis ( newLoader, state, EnterManifest, blob )
  }
}

class Runner extends Actor {

  import Status.Failure

  def receive = {
    case cmd: RunThis =>

      lazy val run = Core run cmd fold (identity, identity)
      sender ! ( try run catch { case t: Throwable => Failure (t) } )

      context stop self
  }
}

