package com.github.bsnisar.tickets.misc

import com.github.bsnisar.tickets.BaseTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StationIdBase64Spec extends BaseTest {

  "A StationIdBase64" should "encode and then decode for departure" in {
    val id = 100054
    val base = new StationIdBase64()

    val resEncode = base.encode(id.toString, isDeparture = true)

    val resDecode = base.decoder(resEncode)
    assert(resDecode.get.id === id.toString)
    assert(resDecode.get.from === true)
  }

  it should "encode and then decode for arrival" in {
    val id = "F1029x0"
    val base = new StationIdBase64()

    val resEncode = base.encode(id.toString, isDeparture = false)
    assert(resEncode.startsWith("/to_"))

    val resDecode = base.decoder(resEncode)
    assert(resDecode.get.id === id.toString)
    assert(resDecode.get.from === false)
  }

}
