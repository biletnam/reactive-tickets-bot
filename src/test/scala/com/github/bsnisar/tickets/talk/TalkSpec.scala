package com.github.bsnisar.tickets.talk

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import com.github.bsnisar.tickets.AkkaBaseTest
import com.github.bsnisar.tickets.telegram.ac.TelegramPush
import com.github.bsnisar.tickets.telegram.{TelegramMessages, TgUpdate}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TalkSpec extends AkkaBaseTest(ActorSystem()) {

  "A Talk" should "delegate stations search" in {
    val tg = TestProbe()
    val search = TestProbe()
    val ref = TestActorRef(Talk.props("1", search.ref, tg.ref))
    ref ! TgUpdate(1, "/from Ochakovo", "123")
    search.expectMsgClass(classOf[TgUpdate])
  }

  it should "send resend command to correct char" in {
    val tg = TestProbe()
    val search = TestProbe()
    val ref = TestActorRef(Talk.props("1", search.ref, tg.ref))
    val msg = TelegramMessages.MsgSimple('test)
    ref ! msg

    tg.expectMsg(TelegramPush.PushMessage("1", msg))
  }
}
