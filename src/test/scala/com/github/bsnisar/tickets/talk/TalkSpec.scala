package com.github.bsnisar.tickets.talk

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.telegram.TelegramUpdates.TgUpdate
import com.github.bsnisar.tickets.{AkkaBaseTest, JMockExpectations}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.util.Success

@RunWith(classOf[JUnitRunner])
class TalkSpec extends AkkaBaseTest(ActorSystem()) {



  "A Talk" should "delegate stations search" in {
    val nf = TestProbe()
    val mockery = newMockery()
    val stationId = mockery.mock(classOf[StationId])
    mockery.checking(new JMockExpectations {
      oneOf(stationId).decode(any[String])
      will(returnValue(Success(StationId.Id("123", from = true))))
    })

    val ref = TestActorRef[Talk](Talk.props("1", stationId, nf.ref))
    ref ! TgUpdate(1, "/from_123", "1")
    nf.expectMsgAnyClassOf(classOf[UpdatesNotifier.AcceptNotify])
  }

}
