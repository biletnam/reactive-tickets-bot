package org.tikets.bot

import akka.actor.ActorSelection
import akka.persistence.PersistentActor
import org.tikets.bot.RoutesTalk.{Evt, Suggestion, Unset}
import org.tikets.misc.Log
import org.tikets.msg.SelectDepartStation



class RoutesTalk(val persistenceId: String) extends PersistentActor with Log {
  private val stations: ActorSelection = context.actorSelection("/stations")
  private val routes: ActorSelection = context.actorSelection("/routes")

  /**
    * Suggestions.
    */
  private var suggestion: Suggestion = Map.empty

  private var state: Evt = Unset

  private def updateState(evt: Evt) = {

  }

  private def onIdle: Receive = {
    case SelectDepartStation(name) =>
      stations ! Stations.FindStationsReq(name)
      context become awaitDepatStationsAnswer
  }

  private def awaitDepatStationsAnswer: Receive = {
    case Stations.StationHits(options) =>
      val suggestion: Map[String, Stations.Station] = options.zipWithIndex.foldLeft(Map.empty[String, Stations.Station]) { (map, pair) =>
        val key = s"/${pair._2}"
        val station = pair._1
        map + (key -> station)
      }
  }

  override def receiveRecover: Receive = ???

  override def receiveCommand: Receive = onIdle

}

object RoutesTalk {

  type Suggestion = Map[String, Stations.Station]

  /**
    * Root event that modify request.
    */
  sealed trait Evt
  case class PickSuggestion(key: String) extends Evt
  case object Unset extends Evt
  case class SuggestionsTo(values: Map[String, Stations.Station]) extends Evt
  case class SuggestionsFrom(values: Map[String, Stations.Station]) extends Evt

}