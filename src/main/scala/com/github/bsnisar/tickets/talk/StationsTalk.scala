package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.talk.StationsTalk.{FindArrival, FindDeparture}
import com.github.bsnisar.tickets.telegram._
import com.github.bsnisar.tickets.telegram.Update
import com.github.bsnisar.tickets.telegram.Update.Text
import com.github.bsnisar.tickets.{Station, Stations}
import com.typesafe.scalalogging.LazyLogging

import scala.util.matching.Regex

object StationsTalkRoute {
  private val StationsSearchFrom: Regex = "^/from\\s+(\\w+|\\W+)".r
  private val StationsSearchTo: Regex = "^/to\\s+(\\w+|\\W+)".r
}


case class StationsTalkRoute(ref: ActorRef) extends RefRouteLogic[Update] {

  lazy val specify: PartialFunction[Update, RouteEvent[Update]] = {
    case update@Text(StationsTalkRoute.StationsSearchFrom(name)) =>
      UpdateEvent(update, FindDeparture(name))
    case update@Text(StationsTalkRoute.StationsSearchTo(name)) =>
      UpdateEvent(update, FindArrival(name))
  }

}



object StationsTalk {
  def props(stations: Stations, stationId: StationId, telegram: ActorRef): Props =
    Props(classOf[StationsTalk], stations, stationId, telegram)


  final case class FindDeparture(name: String)
  final case class FindArrival(name: String)
}

class StationsTalk(val stations: Stations, val stationId: StationId,
                   val telegram: ActorRef) extends Actor with LazyLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {

    case UpdateEvent(update, Some(FindDeparture(name))) =>
      logger.debug(s"on FindDeparture($name)")
      stations.stationsByName(name)
        .map(mkStationsResponse(_, 'from_stations_found, fromStation = true))
        .recover(doRecover(name))
        .map(update.mkReply)
        .pipeTo(self)

    case UpdateEvent(update, Some(FindArrival(name))) =>
      logger.debug(s"on FindArrival($name)")
      stations.stationsByName(name)
        .map(mkStationsResponse(_, 'to_stations_found, fromStation = false))
        .recover(doRecover(name))
        .map(update.mkReply)
        .pipeTo(self)

    case msg: Answers.Reply =>
      telegram ! msg
  }

  private def doRecover(cmd: String): PartialFunction[Throwable, Msg] = {
    case ex =>
      logger.error(s"recover exception from call", ex)
      MsgCommandFailed('cmd_failed, cmd)
  }

  private def mkStationsResponse(hits: Iterable[Station],
                                 templateId: Symbol,
                                 fromStation: Boolean): Msg = hits match {
    case coll if coll.isEmpty =>
      MsgSimple('stations_not_found)
    case coll =>
      val preparedStations = coll.map { station =>
        val encodedId = stationId.encode(id = station.id, fromStation)
        station.copy(id = encodedId)
      }

      MsgFoundStations(
        id = templateId,
        stations = preparedStations
      )
  }
}
