package com.github.bsnisar.tickets.talk

import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import com.github.bsnisar.tickets.{AkkaBaseTest, JMockExpectations}
import com.github.bsnisar.tickets.telegram.TelegramUpdates
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Update
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TalksSpec extends AkkaBaseTest(ActorSystem()) {

  "A Talks" should "create rooms for new comers" in {
    val mockery = newMockery()
    val f = mockery.mock(classOf[Talks.BotFactory])
    val prob = TestProbe()
    mockery.checking(new JMockExpectations {
      oneOf(f).create(any[String])(any[ActorContext])
      will(returnValue(prob.ref))
    })

    val ref = TestActorRef(Talks.props(f))
    val update1 = Update(1, "text", "abc")
    val update2 = Update(2, "text", "abc")
    val update3 = Update(3, "text", "abc")
    ref ! TelegramUpdates.Updates(1, Seq(update1, update2, update3))
    prob.expectMsg(update1)
    prob.expectMsg(update2)
    prob.expectMsg(update3)

    ref ! TelegramUpdates.Updates(1, Seq(update3))
    prob.expectNoMsg()

    mockery.assertIsSatisfied()
  }
}
