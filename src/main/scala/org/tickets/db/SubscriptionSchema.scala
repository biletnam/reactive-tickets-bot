package org.tickets.db

import java.sql
import java.time.LocalDateTime

import slick.driver.H2Driver.api._


object SubscriptionSchema {
  implicit val localDateTimeColumnType = MappedColumnType.base[LocalDateTime, sql.Timestamp](
    { ldt: LocalDateTime => sql.Timestamp.valueOf(ldt) },
    { date: sql.Timestamp => date.toLocalDateTime }
  )


  case class TicketsSubscription(id: Int, hash: String, criteria: String, lastUpdated: LocalDateTime)

  /**
    * Subscription for tickets.
    */
  class TicketsSubscriptions(tag: Tag) extends Table[TicketsSubscription](tag, "SUBSCRIPTION") {
    def id = column[Int]("SUB_ID", O.PrimaryKey, O.AutoInc)
    def hash = column[String]("HASH")
    def criteria = column[String]("CRITERIA")
    def lastUpdated = column[LocalDateTime]("LAST_UPDATED")
    def * = (id, hash, criteria, lastUpdated) <> (
      (TicketsSubscription.apply _).tupled, TicketsSubscription.unapply
      )
  }

  val Subscriptions = TableQuery[TicketsSubscriptions]

  case class Observer(subID: Int, chat: Long)

  /**
    * User, who subscribe for updates.
    */
  class Observers(tag: Tag) extends Table[Observer](tag, "OBSERVER") {
    def subID = column[Int]("SUB_ID")
    def name = column[Long]("OBSERVER_CODE")
    def * = (subID, name) <> ((Observer.apply _).tupled, Observer.unapply)
    def pk = primaryKey("primaryKey", (subID, name))
    def subscription = foreignKey("FK_OBSERVER_SUBSCRIPTION", subID, Subscriptions)(_.id)
  }

  val Observers = TableQuery[Observers]
}
