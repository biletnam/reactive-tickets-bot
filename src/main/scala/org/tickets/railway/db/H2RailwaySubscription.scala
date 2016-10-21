package org.tickets.railway.db

import java.time.LocalDateTime

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.tickets.db.SubscriptionSchema._
import org.tickets.misc.DatabaseSupport._
import org.tickets.misc.LogSlf4j
import org.tickets.model.TrainCriteria
import org.tickets.railway.RailwaySubscription
import slick.driver.H2Driver.api._

import scala.concurrent.{ExecutionContext, Future}

class H2RailwaySubscription(val db: DB)(
  implicit ex: ExecutionContext) extends RailwaySubscription with LogSlf4j {

  override def subscribe(chatID: Long, criteria: TrainCriteria): Future[Observer] = {
    import org.json4s.jackson.Serialization.write
    import org.tickets.misc.JsonSupport._

    val json: String = write(criteria)
    log.trace("[#subscribe] query {}", json)
    val hashVal = Hashing.md5()
      .hashString(json, Charsets.UTF_8)
      .toString

    lazy val findByHash = (for {
      sub <- Subscriptions if sub.hash === hashVal
    } yield sub.id).result.headOption

    lazy val addSubscription =
      Subscriptions += TicketsSubscription(0, hashVal, json, LocalDateTime.now())

    lazy val subscribeObserver =
      (id: Int) => {
        Observers returning Observers += Observer(id, chatID)
      }

    val action = findByHash.flatMap {
      case None =>
        addSubscription.flatMap { genID =>
          subscribeObserver(genID)
        }
      case Some(existID) =>
        subscribeObserver(existID)
    }
    db.run(action)
  }
}


