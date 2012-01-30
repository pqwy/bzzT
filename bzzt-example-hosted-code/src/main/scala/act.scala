package desu

import akka.actor._

class Pongu extends Actor {

  println ("** Initializing Pongu. **")

  def receive = {
    case m =>
      println ("* [PONGU] " + m)
      sender ! m
  }
}

