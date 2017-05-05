package com.github.bsnisar.tickets.provider

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.wire.MockWire
import com.github.bsnisar.tickets.{AkkaBaseTest, Station, Stations}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class UzStationsSpec extends AkkaBaseTest(ActorSystem()) {

  implicit val mt  = ActorMaterializer()

  "An UzStations" should "parse response as stations" in {
    val json =
      """
        |[
        |  {"label": "TestStation", "value": 1001}
        |]
      """.stripMargin

    val wire = new MockWire(json)
    val stations: Stations = new StationsUz(wire)
    val foundStations: Iterable[Station] = Await.result(stations.stationsByName("word"), Duration.Inf)

    assert(foundStations.nonEmpty)
    assert(foundStations.head.id === "1001")
    assert(foundStations.head.name === "TestStation")
  }

}
