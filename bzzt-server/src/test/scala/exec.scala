package xxx.desu.bzzt
package tests

import org.scalatest.FeatureSpec
import org.scalatest.matchers.MustMatchers

import java.lang.reflect.InvocationTargetException


protected [tests]
class RunCoreTests extends FeatureSpec with MustMatchers {

  val (cmdFree, cmdJailed) =

    ( RunThis ( newLoader = new Loaders (isolation = JoinToInvoker)
              , state     = RunCore newDefaultState
              , blob      = scalaJar
              , entry     = null )

    , RunThis ( newLoader = new Loaders (isolation = JoinToSystem )
              , state     = RunCore newDefaultState
              , blob      = javaJar
              , entry     = null )
    )

  def cmd (c : RunThis) (e : EntryPoint) = c copy ( entry = e )

  feature ("run-core invocations") {

    scenario ("the basic setup / explicit method") {
      ( RunCore run cmd (cmdJailed) (EntryClsMeth ("oposum.X", "rondom"))
          must be (Right ("hello, java")) )
      ( RunCore run cmd (cmdFree  ) (EntryClsMeth ("desu.B", "rondom"))
          must be (Right (Map ("hell" -> "yeah!"))) )
    }

    scenario ("the basic setup / apply") {
      ( RunCore run cmd (cmdJailed) (EntryCls ("oposum.X"))
          must be (Right ("hello, java")) )
      ( RunCore run cmd (cmdFree  ) (EntryCls ("desu.B"))
          must be (Right (Map ("hell" -> "yeah!"))) )
    }

    scenario ("the basic setup / manifest") (pending)

    scenario ("inner exceptions") {
      RunCore run cmd (cmdFree) (EntryCls ("desu.C")) match {
        case Left (e: InvocationTargetException) =>
      }
    }
    scenario ("outer exceptions") {
      intercept[ClassNotFoundException] (
        RunCore run cmd (cmdFree) (EntryCls ("no.no.no.no.no"))
      )
    }

  }

  feature ("state") {

    scenario ("priority of state-accepting methods") {
      ( RunCore run cmd (cmdFree) (EntryCls ("desu.Needy"))
          must be (Right ('goodie)) )
    }
    scenario ("get") {
      val rn : java.lang.Double = math.random
      cmdFree.state set rn
      ( RunCore run cmd (cmdFree) (EntryCls ("desu.Gettie"))
          must be (Right (rn)) )
    }
    scenario ("update") {
      val state1 = cmdFree.state.get
      RunCore run cmd (cmdFree) (EntryCls ("desu.Perky"))
      val state2 = cmdFree.state.get
      state1 must not be (state2)
    }
  }

}
