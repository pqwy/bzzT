package xxx.desu.bzzt

import akka.actor._
import akka.dispatch.{ Await, Dispatchers }
import akka.util.duration._
import SupervisorStrategy.{ Stop, Escalate }
import Status.Failure

import java.util.concurrent.atomic.AtomicReference


object ServerStarter {
  def apply (system: ActorSystem) = {
    system actorOf ( Props [RunServ] , name = "run" )
    system actorOf ( Props [AttachServ] , name = "attach" )
  }
}

object Strat {
  def forgetful = OneForOneStrategy () {
    case _ : Exception => Stop
    case _             => Escalate
  }
}

class RunServ extends Actor {

  val (newLoader, state) =
    ( new Loaders ( isolation = JoinToInvoker ), RunCore newDefaultState )

  override val supervisorStrategy = Strat.forgetful

  def receive = decode andThen ( context actorOf (runner) forward _ )

  def decode : PartialFunction [Any, RunThis] = {

    case (cls : String, blob : Array[Byte]) =>
      RunThis ( newLoader, state, EntryCls (cls), blob )

    case (cls : String, meth : String, blob : Array[Byte]) =>
      RunThis ( newLoader, state, EntryClsMeth (cls, meth), blob )

    case blob : Array[Byte] =>
      RunThis ( newLoader, state, EntryManifest, blob )
  }

  val runner = ( Props ( new Actor {

    def receive = {
      case cmd: RunThis =>

        lazy val run = RunCore run cmd fold (identity, identity)
        sender ! ( try run catch { case t: Throwable => Failure (t) } )
        context stop self

    } } ) withDispatcher "akka.actor.pinning-dispatcher"
  )
}

class AttachServ extends Actor {

  override val supervisorStrategy = Strat.forgetful

  def receive = {
    case (cls: String, blob: Array[Byte]) =>

      try {

        val creator = AttachCore attached AttachThis (cls, blob)
        sender ! ( context actorOf (Props ( creator () )) )

      } catch { case t: Throwable => sender ! Failure (t) }
  }


//   def receive = {
//     case (cls: String, blob: Array[Byte]) =>
//       context actorOf (Props (new Runner)) forward AttachThis (cls, blob)
//   }

//   class Runner extends Actor {
//     def receive = {
//       case cmd: AttachThis =>
//         sender ! ( try ( AttachCore attachNewSystem (cmd, context) )
//                     catch { case t: Throwable => Failure (t) } )
//         context stop self
//     }
//   }
}

