package desu

import java.util.concurrent.atomic.AtomicReference

object Single {
  def boo (s: String) { println ( "[%s] deep inside" format s ) }
}

class Pass { def foo { Single.boo (" (>^_^)> ") } }

class A {
  def apply = {
    new Pass foo ;
    Map ("hell" -> "yeah!")
  }
}

class B extends A {
  def rondom = apply
  def smile  = "happy"
}

class C extends A {
  override def apply = {
    super.apply ;
    throw new Exception ("dat x.")
  }
}

import java.lang.Thread

class Hogs (time : Int) {
  def apply = {
    println ("[%d] pre-hog." format time)
    Thread sleep time
    println ("[%d] post-hog." format time)
    'snap
  }
}

class Hog extends Hogs (3000)

class RondoHog
  extends Hogs (math.random * 2000 + 1000 toInt)

class Needy {
  def apply = {
    println ("no reference")
    'sadly
  }
  def apply (ar: AtomicReference[AnyRef]) = {
    println ("Mah reference! " + ar)
    'goodie
  }
}

class Gettie {
  def apply (ar: AtomicReference[AnyRef]) = ar.get
}

class Perky {
  def apply (ar: AtomicReference[AnyRef]) = {
    val nu   = Symbol (math.random.toString)
    val prev = ar getAndSet nu
    println ("[%s]  ->  [%s]" format (prev, nu))
    'ok
  }
}

