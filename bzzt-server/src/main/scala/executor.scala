package xxx.desu.bzzt

import akka.actor._

import scalaz._ ; import Scalaz._

import java.util.concurrent.atomic.AtomicReference


sealed abstract class EntryPoint
case class  EntryCls      (cls : String)                extends EntryPoint
case class  EntryClsMeth  (cls : String, meth : String) extends EntryPoint
case object EntryManifest                               extends EntryPoint

case class RunThis (
    newLoader : Loaders
  , state     : RunCore.ClientState
  , entry     : EntryPoint
  , blob      : Array[Byte]
)

object RunCore {

  type ClientState = AtomicReference [AnyRef]

  def newDefaultState : ClientState = new AtomicReference [AnyRef] ()

  def run (cmd : RunThis) : Either[Throwable, Any] = {

    val loader = cmd.newLoader fromJar cmd.blob

    val (clsn, methn) = cmd entry match {
      case EntryCls      (k)    => (k, "apply")
      case EntryClsMeth  (k, m) => (k, m)
      case EntryManifest        => sys error "not yet..."
    }

    val cls = loader loadClass clsn

    val refClass      = classOf[ClientState]
    val (meth, param) =
      try cls.getMethod (methn, refClass) -> Seq (cmd.state)
      catch { case _ : NoSuchMethodException =>
          cls.getMethod (methn)           -> Seq ()
      }

    val ob = cls newInstance

    try Right ( meth invoke (ob, param : _*) )
    catch { case e: Throwable => Left (e) } 
  }

}

case class AttachThis (
    cls   : String
  , blob  : Array[Byte]
)

object AttachCore {

  val newLoader = new Loaders ( isolation = JoinToInvoker )
  
  def attached (cmd: AttachThis): () => Actor = {

    val loader = newLoader fromJar cmd.blob
    val cls    = (loader loadClass cmd.cls).asInstanceOf[Class[Actor]]
    cls.getConstructor ()

    { () => cls.newInstance }
  }


  // CRAP
  def newName = (
    "xx"
//       ( java.util.UUID.randomUUID toString )
//       replace ("-", "_")
    )

  def attachNewSystem (cmd : AttachThis, ctx: ActorContext) : ActorRef = {

    val loader = newLoader fromJar cmd.blob
    val cls    = (loader loadClass cmd.cls).asInstanceOf[Class[Actor]]
    cls.getConstructor ()

    try {

//       val system = ActorSystem ( newName )
      val system = ctx // ActorSystem ( newName )

      val master   = system actorOf (Props (cls.newInstance), "master")
      val monitor  = system actorOf (Props (new Monitor (master)))
      val reloader = system actorOf (Props (new Reloader (master)), "reload-master")
      println ("** Started " + master)
      master

    } catch { case t : Throwable => println (">> " + t) ; throw t }
  }

  class Monitor (a: ActorRef) extends Actor {
    def receive = {
      case m => println ("[mon] NO, FUCK YOU (" + m + ")")
    }
  }
  class Reloader (a: ActorRef) extends Actor {
    def receive = {
      case m => println ("[rel] NO, FUCK YOU (" + m + ")")
    }
  }
}


// object Instantiator {

//   val newLoader = new Loaders ( isolation = JoinToInvoker )

//   def newInstance [T: Manifest] (
//       className : Option[String]
//     , blobs     : Array[Byte] *) : Class[T] = {

//     val mf     = implicitly[Manifest[T]]

//     val loader = newLoader fromJar (blobs : _*)
//     val cName  = className getOrElse ( sys error "Manifest loading not supported yet." )
//     val cls    = loader loadClass cName

//     if (mf.erasure isAssignableFrom cls) cls.asInstanceOf [Class[T]]
//     else sys error ( "Loaded class `%s' does not match desired type `%s'"
//                         format (cls, mf.erasure) )
//   }

// }

