package org.tikets.bot

import akka.persistence.PersistentActor
import org.tikets.RoutePoint
import org.tikets.bot.Stations.Station
import org.tikets.bot.Talk.TalkState
import org.tikets.msg.Msg

object Talk {

  sealed trait TalkState

  case class AwaitStation(dest: RoutePoint) extends TalkState

  /**
    * Temporary stat that await for answer.
    * @param options match options
    */
  case class StationSelection(options: Map[String, Station]) extends TalkState

}

class Talk extends PersistentActor {
  override def persistenceId: String = "persist-id"

  /**
    *
    */
  private var state : TalkState = null


  override def receiveCommand: Receive = {
    case msg: Msg =>
  }

  override def receiveRecover: Receive = ???



  private def onIdle(msg: Msg) = msg.phrase.command {
    case "/from" =>
    case "/to" =>
  }

  private def awaitStationOpts(value: Any) = {
    case Stations.StationHits(options) =>

  }

}
