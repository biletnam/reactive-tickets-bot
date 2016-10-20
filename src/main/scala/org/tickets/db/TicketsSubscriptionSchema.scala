package org.tickets.db

import java.sql
import java.time.LocalDateTime

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.tickets.db.TicketsSubscriptionSchema._
import org.tickets.misc.DatabaseSupport.DB
import org.tickets.model.TicketsCriteria
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import scala.concurrent.{ExecutionContext, Future}


object TicketsSubscriptionSchema {
  implicit val localDateTimeColumnType = MappedColumnType.base[LocalDateTime, sql.Timestamp](
    { ldt: LocalDateTime => sql.Timestamp.valueOf(ldt) },
    { date: sql.Timestamp => date.toLocalDateTime }
  )


  case class TicketsSubscription(id: Int, hash: String, criteria: String, lastUpdated: LocalDateTime)

  /**
    * Subscription for tickets.
    */
  class TicketsSubscriptions(tag: Tag) extends Table[TicketsSubscription](tag, "SUBSCRIPTION") {
    def id = column[Int]("SUB_ID", O.PrimaryKey, O.AutoInc) // This is the primary key column
    def hash = column[String]("HASH")
    def criteria = column[String]("CRITERIA")
    def lastUpdated = column[LocalDateTime]("LAST_UPDATED")
    def * = (id, hash, criteria, lastUpdated) <> ((TicketsSubscription.apply _).tupled, TicketsSubscription.unapply)
  }

  case class Observer(subID: Int, chat: Long)

  /**
    * User, who subscribe for updates.
    */
  class Observers(tag: Tag) extends Table[Observer](tag, "SUBSCRIBER") {
    def subID = column[Int]("SUB_ID") // Subscription id
    def name = column[Long]("NAME") // Every table needs a * projection with the same type as the table's type parameter
    def * = (subID, name) <> ((Observer.apply _).tupled, Observer.unapply)
    def pk = primaryKey("primaryKey", (subID, name))
    def subscription = foreignKey("FK_SUBSCRIPTION", subID, TableQuery[TicketsSubscriptions])(_.id)
  }

  val Subscriptions = TableQuery[TicketsSubscriptions]
  val Observers = TableQuery[Observers]
}
