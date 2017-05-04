package com.github.bsnisar.tickets.talk

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import com.github.bsnisar.tickets.AkkaBaseTest
import com.github.bsnisar.tickets.telegram.TgUpdate

class RoutesSpec extends AkkaBaseTest(ActorSystem()) {

  "A RoutesSpec" should "routes send to stations talk" in {
    val stationsTalkProb = TestProbe()
    val sut = TestActorRef(Routes.props(new StationsTalkRoute(stationsTalkProb.ref)))

    sut ! TgUpdate(1, "/from Днепр", "0")
    stationsTalkProb.expectMsgAnyClassOf(classOf[UpdateEvent])
  }

}
