package xxx.desu.bzzt
package tests

import org.scalatest.FeatureSpec
import org.scalatest.matchers.MustMatchers

import java.lang.reflect.InvocationTargetException


class CoreTests extends FeatureSpec with MustMatchers {

  val (cmdFree, cmdJailed) =

    ( RunThis ( newLoader = new Loaders (isolation = JoinToInvoker)
              , state     = Core newDefaultState
              , blob      = scalaJar
              , entry     = null )

    , RunThis ( newLoader = new Loaders (isolation = JoinToSystem )
              , state     = Core newDefaultState
              , blob      = javaJar
              , entry     = null )
    )

  def cmd (c : RunThis) (e : EntryPoint) = c copy ( entry = e )

  feature ("core invocations") {

    scenario ("the basic setup / explicit method") {
      ( Core run cmd (cmdJailed) (EnterClsMeth ("oposum.X", "rondom"))
          must be { Right ("hello, java") } )
      ( Core run cmd (cmdFree  ) (EnterClsMeth ("desu.B", "rondom"))
          must be { Right (Map ("hell" -> "yeah!")) } )
    }

    scenario ("the basic setup / apply") {
      ( Core run cmd (cmdJailed) (EnterCls ("oposum.X"))
          must be { Right ("hello, java") } )
      ( Core run cmd (cmdFree  ) (EnterCls ("desu.B"))
          must be { Right (Map ("hell" -> "yeah!")) } )
    }

    scenario ("the basic setup / manifest") (pending)

    scenario ("inner exceptions") {
      Core run cmd (cmdFree) (EnterCls ("desu.C")) match {
        case Left (e: InvocationTargetException) =>
      }
    }
    scenario ("outer exceptions") {
      intercept[ClassNotFoundException] (
        Core run cmd (cmdFree) (EnterCls ("no.no.no.no.no"))
      )
    }

  }

  feature ("state") {

    scenario ("priority of state-accepting methods") {
      ( Core run cmd (cmdFree) (EnterCls ("desu.Needy"))
          must be { Right ('goodie) } )
    }
    scenario ("get") {
      val rn : java.lang.Double = math.random
      cmdFree.state set rn
      ( Core run cmd (cmdFree) (EnterCls ("desu.Gettie"))
          must be { Right (rn) } )
    }
    scenario ("update") {
      val state1 = cmdFree.state.get
      Core run cmd (cmdFree) (EnterCls ("desu.Perky"))
      val state2 = cmdFree.state.get
      state1 must not be { state2 }
    }
  }

}
