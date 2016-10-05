package org.tickets.bot

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest._
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand

class RouteStationsBotSpec extends TestKit(ActorSystem("test")) with FunSuiteLike
  with BeforeAndAfterAll with BeforeAndAfter {

  override def afterAll {
    shutdown()
  }

  test("idle request and ask api for stations") {
    val uz = TestProbe()
    val tg = TestProbe()
    val ref = TestActorRef[RouteStationsBot](RouteStationsBot.props(uz.ref, tg.ref))
    ref ! Bot.ReqDepartureStation("Dn")
    uz expectMsg FindStationsCommand(ref, "Dn")
  }

  test("wait response from api and ask client for interaction") {
    val uz = TestProbe()
    val tg = TestProbe()
    val ref = TestActorRef[RouteStationsBot](RouteStationsBot.props(uz.ref, tg.ref))
    ref ! Bot.ReqDepartureStation("Dn")
    ref ! FindStationsCommand.StationHits(List(Station("0101", "Dn-01")))
    tg expectMsgAllClassOf classOf[Bot.AskDepartureStationsFrom]
  }

  test("wait answer from client") {
    val uz = TestProbe()
    val tg = TestProbe()
    val ref = TestActorRef[RouteStationsBot](RouteStationsBot.props(uz.ref, tg.ref))
    val actor: RouteStationsBot = ref.underlyingActor

    ref ! Bot.ReqDepartureStation("Dn")
    ref ! FindStationsCommand.StationHits(List(Station("0101", "Dn-01")))
    ref ! Bot.ReplyPickStation("id")
    tg expectMsgAllClassOf classOf[Bot.AskDepartureStationsFrom]
  }
}
