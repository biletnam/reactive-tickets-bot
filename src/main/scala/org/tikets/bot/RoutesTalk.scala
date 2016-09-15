package org.tikets.bot

import akka.actor.ActorSelection
import akka.persistence.PersistentActor
import org.tikets.bot.RoutesTalk.{Evt, Unset}
import org.tikets.misc.Log
import org.tikets.msg.{ArriveAt, PickArriveStation, PickDepartStation}



class RoutesTalk(val persistenceId: String) extends PersistentActor with Log {
  private val stations: ActorSelection = context.actorSelection("/stations")
  private val routes: ActorSelection = context.actorSelection("/routes")


  private var state: Evt = Unset

  private def updateState(evt: Evt) = {

  }

  private def idle: Receive = {
    case PickDepartStation(name) =>
      stations ! Stations.FindStationsReq(name)
      context become awaitDepartStationsAnswer
    case PickArriveStation(name) =>
      stations ! Stations.FindStationsReq(name)
      context become awaitArriveStationsAnswer
    case ArriveAt(date) =>
  }

  private def awaitDepartStationsAnswer: Receive = {
    case Stations.StationHits(options) =>
      context become idle
  }

  private def awaitArriveStationsAnswer: Receive = {
    case Stations.StationHits(options) =>
      context become idle
  }

  override def receiveRecover: Receive = ???
  override def receiveCommand: Receive = idle
}

object RoutesTalk {

  /**
    * Root event that modify request.
    */
  sealed trait Evt
  case class PickSuggestion(key: String) extends Evt
  case object Unset extends Evt
  case class SuggestionsTo(values: Map[String, Stations.Station]) extends Evt
  case class SuggestionsFrom(values: Map[String, Stations.Station]) extends Evt

}