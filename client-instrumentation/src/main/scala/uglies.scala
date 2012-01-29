/** A package of dirty, evil hacks.
*/

package xxx.desu.bzzt
package client

import java.lang.{ Thread, ClassNotFoundException, instrument, management }
import instrument.Instrumentation
import management.ManagementFactory
import com.sun.tools.attach.VirtualMachine
import java.net.{URL, URLClassLoader}


/* This little piggy pretended to be a classloader so it could be attached to a thread.
 * Turns out, threads are a scarce global object in the JVM, so when we need to
 * communicate between separately-loaded instances of the same object, there is not much
 * else to do -- and Java Agents *are* loaded separately.
 */
object SpaceShips {

  private class Noah (x: AnyRef, graft: Thread)
    extends ClassLoader (graft.getContextClassLoader) {

      println ("SpaceShips saving to " + graft)
      graft setContextClassLoader this

      def release = {
        val prev = graft.getContextClassLoader
        if (prev == this)
          graft setContextClassLoader getParent
        x
      }
  }

  def put (x: AnyRef, graft: Thread) { new Noah (x, graft) }

  def get [T: Manifest] (graft: Thread) = {
    val loader = graft.getContextClassLoader
    println ("SpaceShips loading from " + graft)
    try Some ( (loader.getClass getMethod "release" invoke loader)
               .asInstanceOf[T] )
    catch { case e: Exception => None }
  }
}

/* Instrument this virtual machine
 */
object GraftAgent extends SelfDiscover {

  // Instrument with the given jar.
  def injectFromJar (jarpath : String) {

    val rmx       = ManagementFactory.getRuntimeMXBean
    val thisjvmid = rmx.getName takeWhile Character.isDigit

    val target =
      try VirtualMachine attach thisjvmid catch {
      case _: NoClassDefFoundError =>
        sys error (
          """Can't instantiate `com.sun.tools.attach.VirtualMachine'
          |... are we sure we have `tools.jar' in classpath?""" stripMargin )
      }

    target loadAgent jarpath
    target.detach
  }

  // Instrument with the jar this very class was loaded from.
  def injectFromThisJar = originatingJar match {
    case Some (jar) => injectFromJar (jar.getPath)
    case None => sys error "Cannot locate my own jar. :("
  }
}


/* Really shitty way to discover the jar we were loaded from.
 * Feel free to shoot me and/or replace this crap.
 *
 * The idea is to begin with the class we ended up as. Then, traverse the chain
 * of classloaders, searching for the lowest URLClassLoader. This is probably
 * what loaded us in the first place. See the jars it claims to load from;
 * for each one, create a new classloader and try to load our very own class
 * from it. If it succeeds, it is _probably_ the jar we came from.
 */
trait SelfDiscover {

  def originatingJar : Option[URL] =
    getClass.getClassLoader match {
      case u : URLClassLoader =>
        containingJar (getClass.getName, u.getURLs)
      case _ => None
    }

  private def containingJar (name: String, jars: Seq[URL]): Option[URL]
    = jars match {
        case Seq (jar, jars1 @ _*) =>
          if ( classInJar (name, jar) ) Some (jar)
          else containingJar (name, jars1)
        case _ => None
    }

  private def classInJar (name: String, jar: URL) =
    try { new URLClassLoader (Array (jar), null) loadClass name ; true }
    catch { case _ : ClassNotFoundException => false
            case _ : NoClassDefFoundError   => false
    }
}

