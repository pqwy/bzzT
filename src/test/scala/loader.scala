package xxx.desu.bzzt
package tests

import java.lang.ClassNotFoundException

import org.scalatest.FeatureSpec
import org.scalatest.matchers.MustMatchers


class LoaderTests extends FeatureSpec with MustMatchers {

  val (newJailed, newFree) =
    ( new Loaders (isolating = true), new Loaders (isolating = false) )

  lazy val loaderFree   = newFree fromJar scalaJar
  lazy val loaderJailed = newJailed fromJar javaJar
  
  feature ("classloader construction") {
    scenario ("from jar byte array") {
      loaderJailed ; loaderFree
    }
  }

  feature ("basic classloader functionality") {
    scenario ("positive resolution") {
      loaderJailed loadClass "oposum.X"
    }
    scenario ("negative resolution") {
      intercept [ClassNotFoundException] ( loaderFree loadClass "desu.XXX" )
    }
    scenario ("delegation") {
      loaderJailed loadClass "javax.swing.JComponent" 
      loaderFree   loadClass "scala.ScalaObject" 
    }
    scenario ("resource location") (pending)
    scenario ("positive resource reading") {
      val stream = loaderJailed getResourceAsStream "oposum/file.txt"
      stream must not be { null }
      JarIO slurp (stream) must be { Array (54, 54, 54, 10) }
    }
    scenario ("negative resource reading") {
      loaderJailed getResourceAsStream "no/wai/for/this" must be { null }
    }
  }

  feature ("visibility separation") {
    val here = "org.scalatest.FeatureSpec"
    scenario ("free classloader chains to the classloader of the constructing instance") {
      loaderFree loadClass here
    }
    scenario ("... while the jailed one does not") {
      intercept [ClassNotFoundException] ( loaderJailed loadClass here )
    }
  }
}

