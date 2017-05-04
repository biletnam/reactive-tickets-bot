package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.github.bsnisar.tickets.provider.StationsUz
import com.github.bsnisar.tickets.wire.MockWire
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, BeforeAndAfterAll, FlatSpecLike, Ignore}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Ignore
@RunWith(classOf[JUnitRunner])
class UzStationsSpec extends TestKit(ActorSystem()) with BaseTest {

  implicit val mt  = ActorMaterializer()

  override protected def afterAll(): Unit = {
    shutdown()
  }

  "An UzStations" should "parse response as stations" in {
    val json =
      """
        |[
        |  {"title": "TestStation", "station_id": 1001}
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
