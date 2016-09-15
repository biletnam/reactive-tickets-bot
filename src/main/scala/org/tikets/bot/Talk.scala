package org.tikets.bot

import akka.actor.{ActorRef, FSM}
import akka.persistence.PersistentActor
import org.tikets.RoutePoint
import org.tikets.bot.Stations.Station
import org.tikets.bot.Talk._
import scala.concurrent.duration._
import org.tikets.msg.{Msg, RouteFromStation}



class Talk(stations: ActorRef, telegram: ActorRef) extends FSM[TalkState, RouteReq] {

  when(Idle) {
    case Event(RouteFromStation(name), _) =>
      stations ! Stations.FindStationsReq(name)
      goto(AskStationFrom)
  }

  when(AskStationFrom) {
    case Event(Stations.StationHits(names), data) =>
      telegram ! Telegram.SelectStation(null)

      goto(AwaitAnswer)  forMax(15 seconds)
  }

}

object Talk {

  sealed trait TalkState
  case object Idle extends TalkState
  case object AskStationFrom extends TalkState
  case object AwaitAnswer extends TalkState
}
