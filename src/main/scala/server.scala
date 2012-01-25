package xxx.desu.bzzloader

import akka.actor._
import akka.dispatch.{ Await, Dispatchers }
import akka.util.duration._

import java.util.concurrent.atomic.AtomicReference


object ServerStarter {
  def apply (system: ActorSystem) = {
    system actorOf (RunServ ( isolating = false ))
  }
}

case class RunThis (
    newLoader : Loaders
  , state     : AtomicReference[AnyRef]
  , klazz     : String
  , blob      : Array[Byte]
)

object RunServ {

  import FaultHandlingStrategy._

  def apply (isolating : Boolean = false) = (

    Props ( new RunServ ( new Loaders (isolating) ) )

    withFaultHandler OneForOneStrategy {
      case _: Exception => Stop
      case _            => Escalate
    } )
}

class RunServ (newLoader : Loaders) extends Actor {

  val state = new AtomicReference [AnyRef] ()

  val runner = (
    Props [Runner]
    withDispatcher "akka.actor.pinning-dispatcher"
  )

  def receive = {
    case cmd @ (k : String, b : Array[Byte]) =>
      context actorOf (runner) forward RunThis (newLoader, state, k, b)
  }

//   override def preStart { println ("+ RunServ") }
//   override def postStop { println ("- RunServ") }
}

class Runner extends Actor {

  import Status._

  def receive = {

    case rc: RunThis =>

      lazy val run = invoke (rc)
      sender ! (try run catch { case t: Throwable => Failure (t) })

      context stop self
  }

  val refClass = classOf[AtomicReference[_]]

  def invoke (rc: RunThis) = {

    val loader     = rc.newLoader fromJar rc.blob
    val klazz      = loader loadClass rc.klazz

    val (meth, param) =
      try klazz.getMethod ("apply", refClass) -> Seq (rc.state)
      catch { case _ : NoSuchMethodException =>
          klazz.getMethod ("apply")           -> Seq ()
      }

    val ob         = klazz newInstance

    try meth invoke (ob, param : _*) catch { case e: Throwable => e } 
  }

//   override def preStart { println ("+ Runner") }
//   override def postStop { println ("- Runner") }
}

