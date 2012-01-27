package xxx.desu.bzzt

import scalaz._ ; import Scalaz._

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.dispatch.Await

import akka.util.Timeout
import akka.util.duration._

import com.typesafe.config.ConfigFactory

import java.io.FileInputStream
import java.lang.reflect.InvocationTargetException


object Client extends App {

  val cfg    = ConfigFactory.load getConfig "netclient"
//   println ("*** " + cfg)
  val system = ActorSystem ("BzzTClient", cfg)

  try {

    val (rem, jar, cls, mmeth) = parseArgs
    val jar1    = JarIO slurp ( new FileInputStream (jar) )
    val message = mmeth fold ( meth => (cls, meth, jar1), (cls, jar1) )
    val remote  = if (rem matches "akka://.*") rem
                  else "akka://BzzTLoader@%s/user/run" format rem

    implicit val sendTimeout = Timeout (10 seconds)

    println ("\n* Sending to " + remote + " ...")
    val future = system.actorFor (remote) ? message
    println ("\n* Sent. ...")

    val res = Await result (future, 300 seconds)
    res match {
      case ITE (e) =>
        println ("\n* Loaded JAR threw exception:\n\n" + e)
        e.printStackTrace
      case r =>
        println (">> " + r)
    }

  } finally { system.shutdown }

  def parseArgs = args match {
    case Array (remote, jar, cls)       => (remote, jar, cls, None)
    case Array (remote, jar, cls, meth) => (remote, jar, cls, Some (meth))
    case _                              =>
      println ( "params : <remote addr> <jar> <class to invoke> [<method to invoke>]" )
      sys exit 1
  }
}

object ITE {
  def unapply (t: Throwable) = t match {
    case ite: InvocationTargetException => Some (ite.getTargetException)
    case _                              => None
  }
}

