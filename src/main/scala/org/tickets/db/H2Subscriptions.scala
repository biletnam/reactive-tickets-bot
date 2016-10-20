package org.tickets.db

import java.sql
import java.time.LocalDateTime

import com.google.common.base.Charsets
import com.google.common.hash.Hashing
import org.tickets.db.DatabaseSchema._
import org.tickets.misc.DatabaseSupport.DB
import org.tickets.model.TicketsCriteria
import slick.driver.H2Driver.api._
import slick.jdbc.meta.MTable

import scala.concurrent.{ExecutionContext, Future}


object DatabaseSchema {
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


  def observersSchema(db: DB)(implicit executionContext: ExecutionContext): Unit = {
    val tlb = MTable.getTables.map(_.map(_.name.name).toSet)
    db.run(tlb)

  }

  def subscriptionsSchema(dB: DB): Unit = {

  }

}

class H2Subscriptions(val db: DB)(implicit ex: ExecutionContext) extends Subscriptions  {


  override def subscribe(user: String, chatID: Long, criteria: TicketsCriteria): Future[TicketsCriteria] = {
    import akka.http.scaladsl.util.FastFuture._

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

}
