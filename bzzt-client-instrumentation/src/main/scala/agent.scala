package xxx.desu.bzzt
package client

import java.lang.instrument.Instrumentation

// object Durr extends App {

// //   Graft.injectSelf

//   Seq (0 to 3: _*) foreach println

//   println ( (new { def prp = 3 }) prp )

//   println ("agent from outside : " + Agent.##)
// //   println (Agent.i)
// //   println (Agent.x)

// }


object Agent {

  def premain (args: String, inst: Instrumentation) = save (inst)

  def agentmain (args: String, inst: Instrumentation) = save (inst)

  def save (i: Instrumentation) = SpaceShips put i

  def restore1 : Option[Instrumentation] = SpaceShips.get [Instrumentation]

  def restore = restore1 orElse { GraftAgent.injectFromThisJar ; restore1 }
}

