package xxx.desu.bzzt

import scalaz._ ; import Scalaz._

import java.util.concurrent.atomic.AtomicReference


import Core.ClientState

case class RunThis (
    newLoader : Loaders
  , state     : ClientState
  , klazz     : String
  , method    : Option[String]
  , blob      : Array[Byte]
)

object Core {

  type ClientState = AtomicReference [AnyRef]

  def newDefaultState : ClientState = new AtomicReference [AnyRef] ()

  def run (cmd : RunThis) : Either[Throwable, Any] = {

    val stClass    = classOf[ClientState]
    val loader     = cmd.newLoader fromJar cmd.blob
    val klazz      = loader loadClass cmd.klazz
    val method     = cmd.method | "apply"

    val (meth, param) =
      try klazz.getMethod (method, stClass) -> Seq (cmd.state)
      catch { case _ : NoSuchMethodException =>
          klazz.getMethod (method)          -> Seq ()
      }

    val ob         = klazz newInstance

    try Right ( meth invoke (ob, param : _*) )
    catch { case e: Throwable => Left (e) } 
  }

}

