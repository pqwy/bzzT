package xxx.desu.bzzloader

import akka.actor._
import akka.dispatch.{ Await, Dispatchers }
import akka.util.duration._
import com.typesafe.config.ConfigFactory

object StandAlones {

  def tagAround [A] (tag: String) (a: => A) = {
    println (tag + " -> ")
    val res = a
    println (tag + ".")
    res
  }

  def withActorSystem [A] (name : String, cfgblock : String) (f : ActorSystem => A) : A = {
    val system = tagAround ("Creating actor system") {
      ActorSystem (name ) // , ConfigFactory.load getConfig cfgblock)
    }
    try f (system) finally {
      tagAround ("Shutting down the actor system") {
        system.shutdown
    } }
  }

  def withServer [A] (srvcfgblock : String) (body: (ActorRef) => A): A =
    withActorSystem ("Servinator", srvcfgblock) { (system) =>
      body ( ServerStarter (system) )
    }

  def mkMsg = {

    import xxx.desu.bzzloader.JarIO

    val (klazz, file) =
      ( "desu.RondoHog", "/home/self/coad/projects/bzzloader/hosted/wat.jar" )

    val blob = JarIO.slurp (new java.io.FileInputStream (file))
    println ("ok, loaded file.")
    (klazz, blob)
  }

  import java.lang.reflect.{ InvocationTargetException => ITE }

  def runClient [A] (server: ActorRef) = {

    val message = mkMsg
    val resps = 1 to 2 map (_ => server ? (message, 4 seconds))

    println ("\n>> responses:")
    resps foreach {
      (fut) =>
        val res = Await.result (fut, 4 seconds) match {
                      case e: ITE    => e.getTargetException.toString
                      case r: AnyRef => r.toString
                    }
        
        println (">>  " + res)
      }
    println ("")
  }
}

import StandAlones._

object StandaloneServer extends App {

  withServer ("netserve") { (_) =>
    println (">> waiting with server")
    java.lang.Thread.sleep (10000)
    println (">> force-shutting down the server")
  }
}


object StandaloneClient extends App {
  withActorSystem ("Client", "netclient") { (system) =>
    runClient (
      system actorFor ("akka://Servinator@127.0.0.1:2552/user/run")
    )
  }
}

object PingPong extends App {
  println ("* PingPong in.")
//   withServer ("localserve") (runClient)
  withServer ("netserve") (runClient)
  println ("* PingPong out.")
}

// object Res1 extends Res with App

// class Res {
//   val loader = getClass.getClassLoader

//   def desu = 
//     println ("mah loader: [%s]" format loader)
//     println ("mah app.conf: [%s]" format
//       loader.getResource ("application.conf")
//     )

//   desu
// }

