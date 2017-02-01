package com.github.bsnisar.tickets


import org.jmock.Mockery
import org.jmock.lib.concurrent.Synchroniser
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


@RunWith(classOf[JUnitRunner])
class StationsDbSpec extends BaseTest {

  private var db: TestDatabase.db.type = _
  override protected def beforeAll(): Unit = {
    db = TestDatabase.db
    val setup = DBIO.seq(
      (StationsDb.Stations.schema ++ StationsDb.StationTranslations.schema).create
    )

    val action = db.run(setup)
    Await.result(action, Duration.Inf)
  }


  "StationsDb" should "find from db by 'LIKE' name" in {
    val mockery = new Mockery {{
      setThreadingPolicy(new Synchroniser)
    }}

    val mockStations: Stations = mockery.mock(classOf[Stations])

    mockery.checking(new JMockExpectations {
      oneOf(mockStations).stationsByName("Dn")
      will(returnValue(Future.successful(Seq(ConsStation("5", "Dn")))))
    })

    val res = Await.result(new StationsDb(mockStations, db).stationsByName("Dn"), Duration.Inf)
    assert(res.nonEmpty === true)
    mockery.assertIsSatisfied()
  }

  it should "fetch not found results and save them in db, next call will hit db record" in {
    val mockery = new Mockery {{
      setThreadingPolicy(new Synchroniser)
    }}

    val mockStations: Stations = mockery.mock(classOf[Stations])

    mockery.checking(new JMockExpectations {
      oneOf(mockStations).stationsByName("Dn")
      will(returnValue(Future.successful(Seq(ConsStation("5", "Dn")))))
    })

    val sut: Stations = new StationsDb(mockStations, db)

    val firstCall = Await.result(sut.stationsByName("Dn"), Duration.Inf)
    assert(firstCall.nonEmpty === true)

    val secondCall = Await.result(sut.stationsByName("Dn"), Duration.Inf)
    assert(secondCall.nonEmpty === true)

    mockery.assertIsSatisfied()
  }

}
