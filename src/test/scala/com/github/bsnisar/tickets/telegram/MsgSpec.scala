package com.github.bsnisar.tickets.telegram

import com.github.bsnisar.tickets.Station
import com.github.bsnisar.tickets.misc.TemplatesFreemarker
import org.junit.Assert
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class MsgSpec extends FlatSpec with  Matchers {

  "A Msg" should "render freemarker template by id MsgStationsFound" in {
    val tpl = new TemplatesFreemarker
    val msg = MsgStationsFound(Msg.StationsFoundFrom, List(Station("03103", "Ololo")), "byName")
    val str = tpl.renderMsg(msg)
    Assert.assertNotNull(str)
  }
}
