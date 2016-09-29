package org.tickets.bot

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestActors, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.tickets.bot.RoutesQuery.{Req, StationSearch}
import org.tickets.bot.uz.StationUz

class RoutesQuerySpec extends TestKit(ActorSystem("test")) with FlatSpecLike with BeforeAndAfterAll with Matchers {

  override def afterAll {
    shutdown()
  }

  "A RoutesQuery " should "understood request, update internal and aks API for stations" in {
    val probe = TestProbe()
    val ref = TestActorRef[RoutesQuery](Props(classOf[RoutesQuery], probe.ref))
    ref ! FindRoutes("Dn", "Lz")
    ref.underlyingActor.stateData shouldEqual Req(from = StationSearch("Dn"), to = StationSearch("Lz"))
    probe expectMsg StationUz.FindStationsReq("Dn")
  }

}
