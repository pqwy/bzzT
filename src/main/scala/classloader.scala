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

class Loaders (isolating : Boolean = false) {

  val parent : ClassLoader =
    if (! isolating) getClass.getClassLoader
    else ClassLoader.getSystemClassLoader

  implicit def readBytes (blob: Array[Byte]) = new ByteArrayInputStream (blob)

  abstract class MapLoader extends ClassLoader (parent) {

      val bytecode : Map[String, Array[Byte]]

      private def makeClass (name : String, blob : Array[Byte])
        = defineClass (name, blob, 0, blob.length)

      override def findClass (name : String)
        = (bytecode get name map (makeClass (name, _))) | super.findClass (name)

      override def getResourceAsStream (name : String)
        = (bytecode get name) | null

      override def toString =
        "MapLoader ( " + shortMods ( bytecode.keys toSeq : _* ) + " )"
  }

  private val re = """([^.])[^.]*\.""" r 

  private def shortMods (as: String*) =
    ( as map (k => re replaceFirstIn (k, "$1.")) take 10 mkString (", ") ) +
      (if (as.length >= 10) ", ..." else "")

  private def unJar [T <% InputStream] (is : T) : Map[String, Array[Byte]] = {

    val jis = new JarInputStream (is)

    val (reSep, reExt) = ("/" r, """\.class$""" r)

    def pathToClassName (path : String)
      = reExt replaceFirstIn (reSep replaceAllIn (path, "."), "")

    JarIO files jis map ( nd => pathToClassName (nd._1) -> nd._2 ) toMap
  }

  def fromJar (bytezs : Array[Byte] *) = new MapLoader {
    val bytecode = (bytezs map (unJar (_)) asMA) sum
  }
}

