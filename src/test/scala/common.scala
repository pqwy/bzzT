package xxx.desu.bzzt

package object tests {

  def loadJar (name : String) = {
    val prefix = "/home/self/coad/projects/bzzT/hosted/"
    JarIO slurp new java.io.FileInputStream ( prefix + name )
  }

  // Assumed to contain some Scala code, but not the lib.
  val scalaJar = loadJar ( "wat.jar" )

  // Assumed to contain some bare-bones Java code.
  val javaJar  = loadJar ( "X.jar" )

}
