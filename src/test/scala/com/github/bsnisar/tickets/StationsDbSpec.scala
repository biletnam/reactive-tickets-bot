package com.github.bsnisar.tickets


import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


@RunWith(classOf[JUnitRunner])
class StationsDbSpec extends BaseTest {

  private var db: TestDatabase.db.type = _
  override protected def beforeAll(): Unit = {
    db = TestDatabase.db
    val setup = DBIO.seq(
      (StationsDb.Stations.schema ++ StationsDb.StationTranslations.schema).create
    )

    val action = db.run(setup)
    Await.ready(action, Duration.Inf)
  }


  "StationsDb" should "find from db by 'LIKE' name" in {
    val mockStations: Stations = (name: String) => {
      Future.successful(Seq(ConsStation("10054D", "Dn-1")))
    }

    new StationsDb(mockStations, db).stationsByName("Dn")
  }

}
