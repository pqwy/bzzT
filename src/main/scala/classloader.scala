package xxx.desu.bzzt

import scala.collection.mutable.ArrayBuffer
import scala.util.regexp._

import scalaz._ ; import Scalaz._

import java.lang.ClassLoader 

import java.io._
import java.util.jar._


object JarIO { /* No Whiskey, though. */

  // "16K should be enough for everybody!"
  def slurp (is : InputStream, readWindow :  Int = 16384) = {
    val (bf, a) = ( new ArrayBuffer[Byte], new Array[Byte] (readWindow) )
    var read = -1
    do {
      read = is read (a, 0, a.length)
      bf ++= (a take read)
    } while (read != -1)
    bf toArray
  }

  def entries (jis: JarInputStream) = new Iterator[JarEntry] {

    import LazyOption.{ some => delay }

    private var next_ = delay (jis.getNextJarEntry)
    def hasNext = next_.get != null
    def next    = Function.const (next_ get) { next_ = delay (jis.getNextJarEntry) }
  }

  def files (jis: JarInputStream) : Iterator[(String, Array[Byte])] = {
    ( entries (jis) filter (! _.isDirectory)
        map (e => ( e.getName -> slurp (jis, readWindow = 4096) )) )
  }
}

sealed abstract class IsolationPolicy
case object JoinToSystem  extends IsolationPolicy
case object JoinToInvoker extends IsolationPolicy

class Loaders ( isolation : IsolationPolicy ) {

  val parent : ClassLoader = isolation match {
    case JoinToSystem  => null
    case JoinToInvoker => getClass.getClassLoader
  }

  implicit def readBytes (blob: Array[Byte]) = new ByteArrayInputStream (blob)

  abstract class MapLoader extends ClassLoader (parent) {

      val bytecode : Map[String, Array[Byte]]

      private def makeClass (name : String, blob : Array[Byte])
        = defineClass (name, blob, 0, blob.length)

      override def findClass (name : String)
        = (bytecode get class2path (name) map (makeClass (name, _))) |
            ( throw new ClassNotFoundException (name) )

      override def getResourceAsStream (name : String)
        = bytecode get name fold (x => x, null)

      override def toString =
        "MapLoader ( " + shorten ( bytecode.keys toSeq : _* ) + " )"
  }

  private def class2path (path : String) =
    ("""\.""".r replaceAllIn (path, "/")) + ".class"

  private val re = """([^/])[^/]*/""" r 

  private def shorten (as: String*) =
    ( as map (k => re replaceFirstIn (k, "$1/")) take 10 mkString (", ") ) +
      (if (as.length >= 10) ", ..." else "")

  private def unJar [T <% InputStream] (is : T) =
    JarIO files (new JarInputStream (is)) toMap

  def fromJar (bytezs : Array[Byte] *) = new MapLoader {
    val bytecode = (bytezs map (unJar (_)) asMA) sum
  }
}

