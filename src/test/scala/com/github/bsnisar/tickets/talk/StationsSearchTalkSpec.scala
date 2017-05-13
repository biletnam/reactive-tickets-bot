package com.github.bsnisar.tickets.talk

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestProbe}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.talk.Answers.{Answer, AnswerBean}
import com.github.bsnisar.tickets.talk.StationsSearchTalk.{FindArrival, FindDeparture}
import com.github.bsnisar.tickets.telegram.{MsgStationsFound, TgUpdate}
import com.github.bsnisar.tickets.{AkkaBaseTest, JMockExpectations, Station, Stations}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class StationsSearchTalkSpec extends AkkaBaseTest(ActorSystem()) with ImplicitSender {
  import org.scalatest.prop.TableDrivenPropertyChecks._

  val commands = Table(
    ("command", "match?"),
    ("/from NY", Some(FindDeparture("NY"))),
    ("/fromNY", None),
    ("/to Paris", Some(FindArrival("Paris"))),
    ("/to       Paris", Some(FindArrival("Paris"))),
    ("/toParis", None),
    ("/from     Lviv", Some(FindDeparture("Lviv"))),
    ("/fromNY MyLove", None),
    ("/from Днепр", Some(FindDeparture("Днепр")))
  )


  "A StationsTalkSpec" should "call stations for param with /from command" in {
    val mockery = newMockery()
    val mockStations = mockery.mock(classOf[Stations])
    val stationId = mockery.mock(classOf[StationId])
    val tg = TestProbe()
    mockery.checking(new JMockExpectations {
      oneOf(mockStations).stationsByName("Dn")
      will(returnValue(Future.successful(Seq(Station("101", "Dn01")))))

      oneOf(stationId).encode("101", from = true)
      will(returnValue("/from_from101"))
    })

    val sut = TestActorRef(StationsSearchTalk.props(mockStations, stationId, tg.ref))
    val update = TgUpdate(1, "/from Dn", "a")
    sut ! UpdateEvent(update, FindDeparture("Dn"))

    tg.expectMsgPF() {
      case AnswerBean("a", _: MsgStationsFound) => true
    }
    mockery.assertIsSatisfied()
  }

  it should "call stations for param with /to command" in {
    val mockery = newMockery()
    val mockStations = mockery.mock(classOf[Stations])
    val stationId = mockery.mock(classOf[StationId])
    val tg = TestProbe()
    mockery.checking(new JMockExpectations {
      oneOf(mockStations).stationsByName("Dn")
      will(returnValue(Future.successful(Seq(Station("101", "Dn01")))))

      oneOf(stationId).encode("101", from = false)
      will(returnValue("/to_to101"))
    })

    val sut = TestActorRef(StationsSearchTalk.props(mockStations, stationId, tg.ref))
    val update = TgUpdate(1, "/to Dn", "a")
    sut ! UpdateEvent(update, FindArrival("Dn"))

    tg.expectMsgPF() {
      case AnswerBean("a", _: MsgStationsFound) => true
    }

    mockery.assertIsSatisfied()
  }

  it should "parse route commands correctly" in {
    val tg = TestProbe()
    val route = new StationsTalkRoute(tg.ref)
    forAll(commands) { (cmd: String, isMatch: Option[Any]) =>
      val result = cmd match {
        case _ if route.specify.isDefinedAt(TgUpdate(1, cmd, "a")) =>
          route.specify(TgUpdate(1, cmd, "a")).payload
        case _ => None
      }

      assert(result == isMatch)
    }
  }

}
