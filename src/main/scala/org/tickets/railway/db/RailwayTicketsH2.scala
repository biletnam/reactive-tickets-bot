package org.tickets.railway.db

import java.time.LocalDateTime

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.tickets.railway.RailwayTickets
import org.tickets.railway.RailwayTickets.{Request, Watch}

import org.tickets.misc.LogSlf4j
import org.tickets.model.TicketsCriteria

import org.tickets.db.TicketsSubscriptionSchema._
import org.tickets.misc.DatabaseSupport._

import scala.concurrent.{ExecutionContext, Future}

class RailwayTicketsH2(val db: DB, val origin: RailwayTickets)(
  implicit ex: ExecutionContext) extends RailwayTickets with LogSlf4j {

  override def subscribe(request: Request): Future[List[RailwayTickets]] = request match {
    case Watch(chatId, criteria) =>
      saveCriteriaSubscription(chatId, criteria)
    case e @ _ => origin.subscribe(e)
  }

  private def saveCriteriaSubscription(chatId: Long, criteria: TicketsCriteria): Future[List[RailwayTickets]] = {

    ???
  }

}

/*

class H2Subscriptions(val db: DB)(implicit ex: ExecutionContext) extends RailwayTickets  {

  override def subscribe(user: String, chatID: Long, criteria:  TicketsCriteria): Future[TicketsCriteria] = {

    val json: String = ""
    val hashVal = Hashing.md5()
      .hashString(json, Charsets.UTF_8)
      .toString

    ???
  }

  private def getWithSameHash(hashVal: String, chatID: Long): Future[Option[Int]] = {
    lazy val findByHash = (for {
      sub <- Subscriptions if sub.hash === hashVal
    } yield sub.id).result.headOption

    lazy val addSubscription =
      Subscriptions += TicketsSubscription(0, hashVal, "", LocalDateTime.now())

    lazy val addObserver =
      (id: Int) => Observers += Observer(id, chatID)

    val action = findByHash.map {
      case None => addSubscription.map { genID =>
        addObserver(genID)
      }
      case Some(existID) =>
        addObserver(existID)
    }

    db.run(action)
    ???
  }

  private def persistCriteria(): Int = {
    1
  }
*/


