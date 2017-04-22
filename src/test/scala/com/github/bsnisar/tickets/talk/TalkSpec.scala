package com.github.bsnisar.tickets.talk

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import com.github.bsnisar.tickets.telegram.{TelegramMessages, TgUpdate}
import com.github.bsnisar.tickets.{BaseTest, JMockExpectations, Station, Stations}
import org.jmock.Mockery
import org.jmock.lib.concurrent.Synchroniser

import scala.concurrent.Future


class TalkSpec extends TestKit(ActorSystem()) with BaseTest {

  private var mockery: Mockery = _

  before {
    mockery = new Mockery {{
      setThreadingPolicy(new Synchroniser)
    }}
  }

  "A TalkSpec" should "parse /from command" in {

    val mockStations = mockery.mock(classOf[Stations])
    mockery.checking(new JMockExpectations {
      oneOf(mockStations).stationsByName("Dn")
      will(returnValue(Future.successful(Seq(Station("501", "Dn")))))
    })

    val tg = TestProbe()
    val sut = TestActorRef(Talk.props(mockStations, tg.ref))
    sut ! TgUpdate(1, "/from Dn", 42343)
    tg.expectMsgAnyClassOf(classOf[TelegramMessages.SendMsg])
  }


  override protected def afterAll(): Unit = {
    shutdown()
  }
}
