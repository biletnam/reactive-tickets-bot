package org.tickets.db

import java.sql
import java.time.LocalDateTime

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.tickets.db.H2Subscriptions.{Subscriber, TicketsSubscriptions}
import org.tickets.misc.DatabaseSupport
import org.tickets.model.TicketsCriteria
import slick.driver.H2Driver.api._

import scala.concurrent.{ExecutionContext, Future}


object H2Subscriptions {
  implicit val localDateTimeColumnType = MappedColumnType.base[LocalDateTime, sql.Timestamp](
    { ldt: LocalDateTime => sql.Timestamp.valueOf(ldt) },
    { date: sql.Timestamp => date.toLocalDateTime }
  )

  case class TicketsSubscription(id: Int, hash: String, criteria: String, lastUpdated: LocalDateTime  )

  /**
    * Subscription for tickets.
    */
  class TicketsSubscriptions(tag: Tag) extends Table[(Int, String, String, LocalDateTime)](tag, "SUBSCRIPTION") {
    def id = column[Int]("SUB_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def hash = column[String]("HASH")
    def criteria = column[String]("CRITERIA")
    def lastUpdated = column[LocalDateTime]("LAST_UPDATED")
    def * = (id, hash, criteria, lastUpdated)
  }

  /**
    * User, who subscribe for updates.
    */
  class Subscriber(tag: Tag) extends Table[(Int, String)](tag, "SUBSCRIBER") {
    def subID = column[Int]("SUB_ID") // Subscription id
    def name = column[String]("NAME") // Every table needs a * projection with the same type as the table's type parameter
    def * = (subID, name)
    def pk = primaryKey("primaryKey", (subID, name))
    def subscription = foreignKey("FK_SUBSCRIPTION", subID, TableQuery[TicketsSubscriptions])(_.id)
  }
}

class H2Subscriptions(implicit ex: ExecutionContext) extends Subscriptions with DatabaseSupport {

  val subscription = TableQuery[TicketsSubscriptions]
  val subscriber = TableQuery[Subscriber]


  override def subscribe(user: String, criteria: TicketsCriteria): Future[TicketsCriteria] = {
    import akka.http.scaladsl.util.FastFuture._

    val json: String = ""
    val hashVal = Hashing.md5()
      .hashString(json, Charsets.UTF_8)
      .toString

    val updates = getWithSameHash(hashVal).fast
      .flatMap { maybeId =>
        val id = maybeId.getOrElse(persistCriteria())
        db.run(subscriber += (id, user))
      }


    updates.fast.map(res => criteria)
  }

  private def getWithSameHash(hashVal: String): Future[Option[Int]] = {
    val alreadyAddedId = (for {
      sub <- subscription if sub.hash === hashVal
    } yield sub.id).result.headOption

    val r = alreadyAddedId.map {
      case Some(id) =>
        id
      case None =>
        val insert = subscription returning subscription.map(_.id) into ((item, id) => id)
        insert.
    }

//    db.run(query.result.headOption)
    ???
  }

  private def persistCriteria(): Int = {
    1
  }

}
