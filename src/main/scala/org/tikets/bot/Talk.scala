package org.tikets.bot

import akka.actor.ActorRef
import akka.persistence.PersistentActor
import org.tikets.bot.Stations.StationHits
import org.tikets.misc.CommandLike
import org.tikets.msg.{FromStation, Msg}


class Talk(stations: ActorRef) extends PersistentActor with CommandLike {

  private var routeReq: Route = null

  private def onIdle: Receive = {
    case FromStation(name) =>
      stations ! Stations.FindStationsLike(name)
      become(askSpecificStation)
  }

  private def askSpecificStation: Receive = {
    case StationHits(opts) =>
  }

  override def receiveRecover: Receive = ???

  override def persistenceId: String = ???

  override def default: Receive = onIdle
}

object Talk {
  sealed trait TalkState
  sealed trait RouteReq
}
