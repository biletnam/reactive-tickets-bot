package org.tickets.bot

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{Assertions, BeforeAndAfterAll, FunSuiteLike}
import org.tickets.railway.RailwayStations
import org.tickets.railway.spy.StationUz
import org.tickets.telegram.{TelegramPush, Update}

import scala.concurrent.Future

class DefineRouteTalkSpec extends TestKit(ActorSystem("test")) with FunSuiteLike with BeforeAndAfterAll with Assertions {

  override protected def afterAll(): Unit = {
    shutdown()
  }

  test("find stations by /from name") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val push = TestProbe()
    val notifier: TelegramNotification = NotifierRef(100, push.ref)
    val stations: RailwayStations = mock[RailwayStations]

    expecting { e => import e._
      oneOf(stations).findStations("Dn")
      will(returnValue(Future.successful(List(StationUz("13431", "Dn-01")))))
    }

    val ref = TestActorRef(DefineRouteTalk.props(stations, notifier))
    ref ! Bot.Cmd("/from Dn", mock[Update])

    push.expectMsgPF() {
      case TelegramPush.TextMsg(100, msg) if msg.contains("/fst_") => true
    }
    cycle.assert()
  }

  test("find stations by /to name") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val push = TestProbe()
    val notifier: TelegramNotification = NotifierRef(100, push.ref)
    val stations: RailwayStations = mock[RailwayStations]

    expecting { e => import e._
      oneOf(stations).findStations("Dn")
      will(returnValue(Future.successful(List(StationUz("13431", "Dn-01")))))
    }

    val sut = TestActorRef(DefineRouteTalk.props(stations, notifier))
    sut ! Bot.Cmd("/to Dn", mock[Update])

    push.expectMsgPF() {
      case TelegramPush.TextMsg(100, msg) if msg.contains("/tst_") => true
    }
    cycle.assert()
  }

  test("parse 'from' station command-id") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val notifier: TelegramNotification = mock[TelegramNotification]
    val stations: RailwayStations = mock[RailwayStations]

    val sut = TestActorRef(DefineRouteTalk.props(stations, notifier))

    expecting { e => import e._
      oneOf(stations).station("21bba0")
      will(returnValue(Future.successful(StationUz("13431", "Dn-01"))))
    }

    expecting { e => import e._
      oneOf(notifier) << withArg(aNonNull[String])
    }

    sut ! Bot.Cmd("/fst_21bba0", mock[Update])
    cycle.assert()
  }

  test("parse 'to' station command-id") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val notifier: TelegramNotification = mock[TelegramNotification]
    val stations: RailwayStations = mock[RailwayStations]

    val sut = TestActorRef(DefineRouteTalk.props(stations, notifier))

    expecting { e => import e._
      oneOf(stations).station("21bba0")
      will(returnValue(Future.successful(StationUz("13431", "Dn-01"))))
    }

    expecting { e => import e._
      oneOf(notifier) << withArg(aNonNull[String])
    }

    sut ! Bot.Cmd("/tst_21bba0", mock[Update])
    cycle.assert()
  }

  test("parse '/arriveTo' command") {
    val cycle = JMockSupport.threadSafe
    import cycle._

    val notifier: TelegramNotification = mock[TelegramNotification]
    val stations: RailwayStations = mock[RailwayStations]

    val sut = TestActorRef(DefineRouteTalk.props(stations, notifier))

    expecting { e => import e._
      oneOf(notifier) << withArg(aNonNull[String])
    }

    sut ! Bot.Cmd("/arriveTo 10-12-16", mock[Update])
    cycle.assert()
  }



}
