package org.tickets.actors

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{Assertions, BeforeAndAfterAll, FunSuiteLike}
import org.tickets.railway.RailwayStations
import org.tickets.model.Station
import org.tickets.telegram.{Message, TelegramPush}

import scala.concurrent.Future

class DefineRouteTalkSpec extends TestKit(ActorSystem("test")) with FunSuiteLike with BeforeAndAfterAll with Assertions {

  override protected def afterAll(): Unit = {
    shutdown()
  }

  test("find stations by /from name") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val push = TestProbe()
    val notifier: Notifier = NotifierRef(100, push.ref)
    val stations: RailwayStations = mock[RailwayStations]

    expecting { e => import e._
      oneOf(stations).findStations("Dn")
      will(returnValue(Future.successful(List(Station.mock("13431", "Dn-01")))))
    }

    val ref = TestActorRef(DefineRouteTalk.props(stations, notifier))
    ref ! Bot.Cmd("/from Dn", mock[Message])

    push.expectMsgPF() {
      case TelegramPush.TextMsg(100, msg) if msg.contains("/st_") => true
    }
    cycle.assert()
  }

  test("find stations by /to name") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val push = TestProbe()
    val notifier: Notifier = NotifierRef(100, push.ref)
    val stations: RailwayStations = mock[RailwayStations]

    expecting { e => import e._
      oneOf(stations).findStations("Dn")
      will(returnValue(Future.successful(List(Station.mock("13431", "Dn-01")))))
    }

    val sut = TestActorRef(DefineRouteTalk.props(stations, notifier))
    sut ! Bot.Cmd("/to Dn", mock[Message])

    push.expectMsgPF() {
      case TelegramPush.TextMsg(100, msg) if msg.contains("/st_") => true
    }
    cycle.assert()
  }

  test("parse '/arriveTo' command") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val notifier: Notifier = mock[Notifier]
    val stations: RailwayStations = mock[RailwayStations]

    val sut = TestActorRef(DefineRouteTalk.props(stations, notifier))

    expecting { e => import e._
      oneOf(notifier) << withArg(aNonNull[String])
    }

    sut ! Bot.Cmd("/arriveTo 10-12-16", mock[Message])
    cycle.assert()
  }



}
