package com.github.bsnisar.tickets.talk

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import com.github.bsnisar.tickets.{AkkaBaseTest, JMockExpectations}
import com.github.bsnisar.tickets.misc.StationIdBase64
import com.github.bsnisar.tickets.talk.ResponsesSender.Reply
import com.github.bsnisar.tickets.telegram.Update
import org.junit.runner.RunWith
import org.scalatest.Ignore
import org.scalatest.junit.JUnitRunner

@Ignore
@RunWith(classOf[JUnitRunner])
class SearchingRequestTalkSpec extends AkkaBaseTest(ActorSystem()) {



  "A Talk" should "parse date specify cmd" in {
    val tg = TestProbe()

    val mockery = newMockery()
    val update = mockery.mock(classOf[Update])
    mockery.checking(new JMockExpectations {
      allowing(update).text
      will(returnValue("/arrive 2017-05-10"))
    })

    val stationId = new StationIdBase64

    val searchingRequestTalkRef = TestActorRef[SearchingRequestTalk](SearchingRequestTalk.props("1", tg.ref))
    val router = TestActorRef[UpdatesRouter](UpdatesRouter.props(
      new TalkRoute(searchingRequestTalkRef, stationId)
    ))

    router ! update

    tg.expectMsgAnyClassOf(classOf[Reply])
  }

}
