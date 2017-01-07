package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.TestKit
import com.github.bsnisar.tickets.wire.MockWire
import org.testng.Assert
import org.testng.annotations.{AfterSuite, Test}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UzStationsTest extends TestKit(ActorSystem()) {

  implicit val mt  = ActorMaterializer()

  @AfterSuite
  def after(): Unit = {
    shutdown()
  }

  @Test
  def parseResponseAsStations(): Unit = {
    val json =
      """
        |[
        |  {"title": "TestStation", "station_id": 1001}
        |]
      """.stripMargin

    val wire = new MockWire(json)
    val stations: Stations = new UzStations(wire)
    val foundStations: Iterable[Station] = Await.result(stations.stationsByName("word"), Duration.Inf)

    Assert.assertTrue(foundStations.nonEmpty)
  }

}
