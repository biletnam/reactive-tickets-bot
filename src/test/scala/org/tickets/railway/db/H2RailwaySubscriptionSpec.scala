package org.tickets.railway.db

import java.time.LocalDate

import org.scalatest.{Assertions, BeforeAndAfterAll, FunSuite}
import org.tickets.misc.DatabaseSupport
import org.tickets.model.{Station, TrainCriteria}
import org.tickets.railway.RailwaySubscription

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class H2RailwaySubscriptionSpec extends FunSuite with BeforeAndAfterAll with Assertions {

  var db: DatabaseSupport.DB = _

  override protected def beforeAll(): Unit = {
    db = DatabaseSupport.loadDatabase
  }

  override protected def afterAll(): Unit = {
    db.close()
  }

  test("test adding subscription") {
    import scala.concurrent.ExecutionContext.Implicits.global
    val sut: RailwaySubscription = new H2RailwaySubscription(db)

    val criteria: TrainCriteria = TrainCriteria.newCriteria(
      Station("a1", "100", "Dn", "uz"),
      Station("a1", "100", "Dn", "uz"),
      List(LocalDate.now()))

    val insert = sut.subscribe(1001L, criteria)
    assert(Await.ready(insert, Duration.Inf).isCompleted)
  }

}
