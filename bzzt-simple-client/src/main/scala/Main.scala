package xxx.desu.bzzt

import akka.actor._
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask
import akka.dispatch.Await

class Client (system: ActorSystem) {

  implicit val timeout = Timeout (15 seconds)

  def probe (
      jarpath: String, remote: String, klazz: String, meth: String
    ) = sr ( remote, message = (klazz, meth, loadjar (jarpath)) )

  def probe (
      jarpath: String, remote: String, klazz: String
    ) = sr ( remote, message = (klazz, loadjar (jarpath)) )

  def loadjar (path: String) = {
    val is = new java.io.RandomAccessFile (path, "r")
    val a  = new Array[Byte] (is.length.toInt)
    is readFully a ; a
  }

  def sr (remote : String, message : AnyRef) {
    val server = system actorFor remote
    val future = server ? message 
    println (">> [sent]  " + future)
    println (">> " + ( Await result ( future, 15 seconds ) ))
  }
}

object Client extends App {

  val system = ActorSystem ("A_Client")

  try {

    val client = new Client (system)

    args match {
      case Array (jar, remote, klazz, method) =>
        client probe (jar, remote, klazz, method)
      case Array (jar, remote, klazz) =>
        client probe (jar, remote, klazz)
      case _ =>
        println ("args: <jar path> <remote addr> <class> [<method>]")
    }

  } finally system.shutdown

}

