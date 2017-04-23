package com.github.bsnisar.tickets.talk

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef}
import com.github.bsnisar.tickets.talk.StationsSearch.StationsSearchCommands
import com.github.bsnisar.tickets.telegram.{TelegramMessages, TgUpdate}
import com.github.bsnisar.tickets.{AkkaBaseTest, JMockExpectations, Station, Stations}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class StationsSearchSpec extends AkkaBaseTest(ActorSystem()) with ImplicitSender {
  import org.scalatest.prop.TableDrivenPropertyChecks._

  val commands = Table(
    ("command", "match?"),
    ("/from NY", true),
    ("/fromNY", false),
    ("/to Paris", true),
    ("/to       Paris", true),
    ("/toParis", false),
    ("/from     NY", true),
    ("/fromNY MyLove", false)
  )

  "A StationsSearch" should "call stations for param with /from command" in {
    val mockery = newMockery()
    val mockStations = mockery.mock(classOf[Stations])
    val stationId = mockery.mock(classOf[StationId])
    mockery.checking(new JMockExpectations {
      oneOf(mockStations).stationsByName("Dn")
      will(returnValue(Future.successful(Seq(Station("101", "Dn01")))))

      oneOf(stationId).encode("101", from = true)
      will(returnValue("/goto_from101"))
    })

    val sut = TestActorRef(StationsSearch.props(mockStations, stationId))
    sut ! TgUpdate(1, "/from Dn", "a")

    expectMsgClass(classOf[TelegramMessages.MsgFoundStations])
    mockery.assertIsSatisfied()
  }

  it should "call stations for param with /to command" in {
    val mockery = newMockery()
    val mockStations = mockery.mock(classOf[Stations])
    val stationId = mockery.mock(classOf[StationId])
    mockery.checking(new JMockExpectations {
      oneOf(mockStations).stationsByName("Dn")
      will(returnValue(Future.successful(Seq(Station("abcd1", "Dn01")))))

      oneOf(stationId).encode("abcd1", from = false)
      will(returnValue("/goto_to101"))
    })

    val sut = TestActorRef(StationsSearch.props(mockStations, stationId))
    sut ! TgUpdate(1, "/to Dn", "1")

    mockery.assertIsSatisfied()
    expectMsgClass(classOf[TelegramMessages.MsgFoundStations])
  }

  it should "match commands correctly" in {
    forAll(commands) { (cmd: String, isMatch: Boolean) =>
      val result = cmd match {
        case StationsSearch.StationsSearchCommands(_ *) => true
        case _ => false
      }

      assert(result == isMatch)
    }
  }
}
