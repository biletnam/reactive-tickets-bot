package org.tickets.bot

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import org.tickets.bot.RoutesBot.Departure
import org.tickets.uz.cmd.FindStationsCommand

class RoutesBotSpec extends TestKit(ActorSystem("test")) with FunSuiteLike
  with BeforeAndAfterAll with Matchers {

  override def afterAll {
    shutdown()
  }

  test("handle client ask for station and call uz api for details") {
    val uz = TestProbe()
    val tg = TestProbe()
    val ref = TestActorRef(RoutesBot.props(uz.ref, tg.ref))
    ref ! ReqRouteDeparture("Dn")
    uz expectMsg FindStationsCommand(ref, "Dn")
  }

  test("handle api response for station and aks client for selection") {
    val uz = TestProbe()
    val tg = TestProbe()
    val ref = TestActorRef[RoutesBot](RoutesBot.props(uz.ref, tg.ref))
    ref.underlyingActor.context.become(
      ref.underlyingActor.waitStationsSearchOf(Departure, RoutesBot.State.initial)
    )
  }

}
