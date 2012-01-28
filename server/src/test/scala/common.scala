package xxx.desu.bzzt

package object tests {

  def loadJar (name : String) =
    JarIO slurp (getClass.getClassLoader getResourceAsStream name)

  // Assumed to contain some bare-bones Java code.
  val javaJar  = loadJar ( "X.jar" )

  // Assumed to contain some Scala code, but not the lib.
  val scalaJar = loadJar ( "Y.jar" )

}
