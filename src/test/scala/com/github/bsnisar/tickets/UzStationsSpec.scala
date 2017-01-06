package com.github.bsnisar.tickets

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}

@RunWith(classOf[JUnitRunner])
class UzStationsSpec extends  FunSuiteLike with BeforeAndAfterAll with Matchers  {

  override protected def afterAll(): Unit = {
//    shutdown()
  }

//  implicit val mt: Materializer = ActorMaterializer()


  test("parse response as objects with Station contract") {
    val json = """
      |[
      |  {"title": "TestStation", "station_id": 1001}
      |]
    """.stripMargin

//    val wire = new MockWire(json)
//    val stations: Stations = new UzStations(wire)
//    val foundStations: Iterable[Station] = Await.result(stations.stationsByName("word"), Duration.Inf)
//    foundStations.nonEmpty shouldBe true

    fail(s"$json")
  }

}
