package xxx.desu.bzzt
package tests

import org.scalatest.Suites


class FullSuite extends Suites (
    new LoaderTests
  , new ExecTests
  , new AkkaTests
)
