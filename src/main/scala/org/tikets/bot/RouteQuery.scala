package org.tikets.bot

import java.time.LocalDate

import akka.actor.{ActorRef, ActorSelection}
import akka.persistence.PersistentActor
import org.tikets.bot.RouteQuery.Req
import org.tikets.bot.Stations.Station
import org.tikets.misc.Log
import org.tikets.msg.Phrase



class RouteQuery(val persistenceId: String, val telegram: ActorRef) extends PersistentActor with Log {
  private val stations: ActorSelection = context.actorSelection("/stations")
  private val routes: ActorSelection = context.actorSelection("/routes")

  private var req : List[Req] = Nil


  override def receiveCommand: Receive = {
    case ph: Phrase =>
      ph.param("from")
      ph.param("to")
      ph.param("arrive")
  }

  private def stationFrom(ph: Phrase): Unit = ph.param("from") match {
    case name :: Nil =>
      stations ! Stations.FindStationsReq(name)
    case _ =>
      log.error("RouteQuery#stationFrom({}): wrong value 'from'", ph)
  }


  override def receiveRecover: Receive = ???
}

object RouteQuery {

  /**
    * Root event that modify request.
    */
  sealed trait Req

  /**
    * Possible choices for departure station. Need clarification from client.
    * @param choices choices
    */
  case class DepartureStationOpts(choices: Map[String, Station]) extends Req

  /**
    * Possible choices for arrive station. Need clarification from client.
    * @param choices choices
    */
  case class ArriveStationOpts(choices: Map[String, Station]) extends Req

  /**
    * Pick station by its key.
    * @param key station key.
    */
  case class PickStation(key: String) extends Req


  /**
    * Defined station for route.
    * @param station station
    * @param departure is station for departure or for arrival?
    */
  case class RouteStation(station: Station, departure: Boolean) extends Req

  /**
    * Arrive at time
    * @param date arrive at.
    */
  case class ArriveAt(date: LocalDate) extends Req
}