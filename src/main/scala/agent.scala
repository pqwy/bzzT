package xxx.desu.bzzt

import scalaz._ ; import Scalaz._

import java.util.concurrent.atomic.AtomicReference


import Core.ClientState

sealed abstract class EntryPoint
case class  EnterCls      (cls : String)                extends EntryPoint
case class  EnterClsMeth  (cls : String, meth : String) extends EntryPoint
case object EnterManifest                               extends EntryPoint

case class RunThis (
    newLoader : Loaders
  , state     : ClientState
  , entry     : EntryPoint
  , blob      : Array[Byte]
)

object Core {

  type ClientState = AtomicReference [AnyRef]

  def newDefaultState : ClientState = new AtomicReference [AnyRef] ()

  def run (cmd : RunThis) : Either[Throwable, Any] = {

    val loader = cmd.newLoader fromJar cmd.blob

    val (clsn, methn) = cmd entry match {
      case EnterCls      (k)    => (k, "apply")
      case EnterClsMeth  (k, m) => (k, m)
      case EnterManifest        => sys error "not yet..."
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

