package com.github.bsnisar.tickets.talk

import com.github.bsnisar.tickets.BaseTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CmdSpec extends BaseTest {

  "A Cmd" should "parse input command" in {
    val str = "/hello "
    val cmd = new Simple(str)
    assert(cmd.command === "/hello")
    assert(cmd.value === Nil)
  }

  it should "parse values" in {
    val str = "/hello inp1 inp2"
    val cmd = new Simple(str)
    assert(cmd.value === "inp1" :: "inp2" :: Nil)
  }



}
