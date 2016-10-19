package org.tickets.bot

import akka.actor.Actor
import org.tickets.bot.TicketsWatcher.{SearchReq, TrainsSubscription, TrainsSubscriptionRow}
import org.tickets.model.TicketsCriteria
import scalikejdbc._

object TicketsWatcher {

  /**
    * Find tickets.
    * @param user user who need tickets
    * @param criteria criteria for search
    */
  case class SearchReq(user: String, criteria: TicketsCriteria)


  case class TrainsSubscription(id: Int, user: String, query: String)


  protected object TrainsSubscriptionRow extends SQLSyntaxSupport[TrainsSubscription] {
    def apply(e: ResultName[TrainsSubscription])(rs: WrappedResultSet): TrainsSubscription
      = TrainsSubscription(id = rs.get(e.id), user = rs.get(e.name), query = rs.get(e.query))
  }

}

class TicketsWatcher extends Actor  {
  override def receive: Receive = ???



  private def addSubscription(req: SearchReq): Unit = {
    val column = TrainsSubscriptionRow.column
    val query = ""

    DB localTx { implicit session =>
      insert.into(TrainsSubscriptionRow)
        .namedValues(
          column.user -> req.user,
          column.query -> query)
    }
  }



}
