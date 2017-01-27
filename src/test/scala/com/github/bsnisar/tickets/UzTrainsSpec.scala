package com.github.bsnisar.tickets

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UzTrainsSpec extends BaseTest {

  "A UzTrains" should "understand query" in {
    val q = <routes>
      <route from="10" to="5">
        <arrives>
          <arrive>2007-12-03T10:15:30+01:00[Europe/Paris]</arrive>
          <arrive>2007-12-03T11:15:30+01:00[Europe/Paris]</arrive>
        </arrives>
      </route>
    </routes>

    new UzTrains().find(q)
  }

}
