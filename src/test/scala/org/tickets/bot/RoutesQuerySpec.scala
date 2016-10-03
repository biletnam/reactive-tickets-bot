package org.tickets.bot

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestFSMRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.tickets.bot.RoutesQuery._
import org.tickets.bot.uz.FindStationsCommand.StationHits
import org.tickets.bot.uz.{FindStationsCommand, Station}

class RoutesQuerySpec extends TestKit(ActorSystem("test")) with FlatSpecLike
  with BeforeAndAfterAll with Matchers {

  override def afterAll {
    shutdown()
  }

  "A RoutesQuery " should "understood request, update internal and aks API for stations" in {
    val stationsRef = TestProbe()
    val telegramRef = TestProbe()
    val ref = TestActorRef[RoutesQuery](Props(classOf[RoutesQuery], stationsRef.ref, telegramRef.ref))
    ref ! FindRoutes("Dn", "Lz")
    ref.underlyingActor.stateData shouldEqual Req(from = StationSearch("Dn"), to = StationSearch("Lz"))
    stationsRef expectMsg FindStationsCommand(ref, "Dn")
  }

  it should " await answer for station " in {
    val stationsRef = TestProbe()
    val telegramRef = TestProbe()
    val ref = TestActorRef[RoutesQuery](Props(classOf[RoutesQuery], stationsRef.ref, telegramRef.ref))
    ref ! FindRoutes("Dn", "Lz")
    ref ! StationHits(Station("id", "name") :: Station("id2", "name2") :: Nil)
    ref.underlyingActor.stateName shouldEqual FromStationSearchAsk
    stationsRef expectMsg FindStationsCommand(ref, "Dn")
  }

  it should " await response from client " in {
    val stationsRef = TestProbe()
    val telegramRef = TestProbe()
    val fsm = TestFSMRef(new RoutesQuery(stationsRef.ref, telegramRef.ref))
    val variants = Map("b1F" -> Station("1", "A"), "b2F" -> Station("1", "A"))
    fsm.setState(
      stateName = FromStationSearchAsk,
      stateData = Req(StationSearchMatches(variants))
    )

    fsm ! "b1F"
    fsm.stateData shouldBe Req(StationDef(Station("1", "A")))
    fsm.stateName shouldBe DefQuery
  }

}
