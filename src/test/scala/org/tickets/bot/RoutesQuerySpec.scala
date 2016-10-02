package org.tickets.bot

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.tickets.bot.RoutesQuery.{FromStationSearchAsk, Req, StationSearch}
import org.tickets.bot.uz.FindStationsCommand.{FindStations, StationHits}
import org.tickets.bot.uz.Station

class RoutesQuerySpec extends TestKit(ActorSystem("test")) with FlatSpecLike with BeforeAndAfterAll with Matchers {

  override def afterAll {
    shutdown()
  }

  "A RoutesQuery " should "understood request, update internal and aks API for stations" in {
    val probe = TestProbe()
    val ref = TestActorRef[RoutesQuery](Props(classOf[RoutesQuery], probe.ref))
    ref ! FindRoutes("Dn", "Lz")
    ref.underlyingActor.stateData shouldEqual Req(from = StationSearch("Dn"), to = StationSearch("Lz"))
    probe expectMsg FindStations("Dn")
  }

  it should " await answer for station " in {
    val probe = TestProbe()
    val ref = TestActorRef[RoutesQuery](Props(classOf[RoutesQuery], probe.ref))
    ref ! FindRoutes("Dn", "Lz")
    ref ! StationHits(Station("id", "name") :: Station("id2", "name2") :: Nil)
    ref.underlyingActor.stateName shouldEqual FromStationSearchAsk
    probe expectMsg FindStations("Dn")
  }

}
