package com.github.bsnisar.tickets

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UzTrainsSpec extends BaseTest {

  "A UzTrains" should "understand query" in {
    val q = <routes>
      <route from="10" to="5">
        <arrives>
          <arrive>100</arrive>
          <arrive>500</arrive>
        </arrives>
      </route>
    </routes>

    new UzTrains().find(q)
  }

}
