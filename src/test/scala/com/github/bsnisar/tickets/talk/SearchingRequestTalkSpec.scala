package com.github.bsnisar.tickets.talk

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import com.github.bsnisar.tickets.misc.StationIdBase64
import com.github.bsnisar.tickets.talk.Answers.Answer
import com.github.bsnisar.tickets.telegram.{MsgQueryUpdate, TgUpdate, Update}
import com.github.bsnisar.tickets.{AkkaBaseTest, JMockExpectations}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SearchingRequestTalkSpec extends AkkaBaseTest(ActorSystem()) {
  import org.scalatest.prop.TableDrivenPropertyChecks._

  val commands = Table(
    ("command", "match?"),
    ("/departure 2017-05-10", Some(TalkRoute.DepartureTimeCmd("2017-05-10"))),
    ("/arrive 2017-05-11", Some(TalkRoute.ArrivalTimeCmd("2017-05-11")))
  )


  it should "parse arrive date specified in cmd" in {
    val tg = TestProbe()

    // scalastyle:off magic.number
    val date = LocalDate.of(2017, 5, 10)
    val mockery = newMockery()
    val update = mockery.mock(classOf[Update])
    val answer = mockery.mock(classOf[Answer])

    mockery.checking(new JMockExpectations {
      oneOf(update).createAnswer(matchPF {
        case MsgQueryUpdate(bean) if bean.arrive.head == date => true
      })

      will(returnValue(answer))
    })

    val searchingRequestTalkRef = TestActorRef[SearchingRequestTalk](SearchingRequestTalk.props("1", tg.ref))
    searchingRequestTalkRef ! UpdateEvent(update, TalkRoute.ArrivalTimeCmd("2017-05-10"))

    mockery.assertIsSatisfied()
  }


  it should "parse departure date specified in cmd" in {
    val tg = TestProbe()

    // scalastyle:off magic.number
    val date = LocalDate.of(2017, 6, 10)
    val mockery = newMockery()
    val update = mockery.mock(classOf[Update])
    val answer = mockery.mock(classOf[Answer])

    mockery.checking(new JMockExpectations {
      oneOf(update).createAnswer(matchPF {
        case MsgQueryUpdate(bean) if bean.departure.head == date => true
      })

      will(returnValue(answer))
    })

    val searchingRequestTalkRef = TestActorRef[SearchingRequestTalk](SearchingRequestTalk.props("1", tg.ref))
    searchingRequestTalkRef ! UpdateEvent(update, TalkRoute.DepartureTimeCmd("2017-06-10"))

    mockery.assertIsSatisfied()
  }

  it should "route dates to appropriate cmd" in {
    val prob = TestProbe()
    val stationId = new StationIdBase64
    val route = new TalkRoute(prob.ref, stationId)
    forAll(commands) { (cmd: String, check: Option[TalkRoute.Cmd]) =>
      val result = cmd match {
        case _ if route.specify.isDefinedAt(TgUpdate(1, cmd, "a")) =>
          route.specify(TgUpdate(1, cmd, "a")).payload
        case _ => None
      }

      assert(check === result)
    }
  }

}
