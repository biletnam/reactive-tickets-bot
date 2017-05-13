package com.github.bsnisar.tickets.talk

import akka.actor.{ActorContext, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import com.github.bsnisar.tickets.telegram.TgUpdate
import com.github.bsnisar.tickets.{AkkaBaseTest, JMockExpectations}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
// scalastyle:off magic.number
class TalksHubSpec extends AkkaBaseTest(ActorSystem()) {

  "A Talks" should "create rooms for new comers" in {
    val mockery = newMockery()
    val f = mockery.mock(classOf[TalksHub.BotFactory])
    val prob = TestProbe()
    mockery.checking(new JMockExpectations {
      oneOf(f).create(any[String], any[String])(any[ActorContext])
      will(returnValue(prob.ref))
    })

    val ref = TestActorRef(TalksHub.props(f))
    val update1 = TgUpdate(1, "text", "abc")
    ref ! update1
    prob.expectMsg(update1)

    val update2 = TgUpdate(2, "text", "abc")
    ref ! update2
    prob.expectMsg(update2)

    val update3 = TgUpdate(3, "text", "abc")
    ref ! update3
    prob.expectMsg(update3)

    mockery.assertIsSatisfied()
  }
}
