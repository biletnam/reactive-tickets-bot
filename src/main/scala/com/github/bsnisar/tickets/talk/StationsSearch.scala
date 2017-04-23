package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, Props}
import com.github.bsnisar.tickets.telegram.{TelegramMessages, TgUpdate}
import com.github.bsnisar.tickets.{Station, Stations}
import com.typesafe.scalalogging.LazyLogging


object StationsSearch {
  def props(stations: Stations, stationId: StationId): Props =
    Props(classOf[StationsSearch], stations, stationId)
}

class StationsSearch(val stations: Stations, val stationId: StationId) extends Actor with LazyLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case update: TgUpdate =>
      logger.debug(s"try understand ( ${update.text} )")
      val words = update.text.split(" ").toList
      val clientChat = sender()

      words match {
        case (cmd@"/from") :: name :: _ =>
          stations.stationsByName(name)
            .map(mkStationsResponse(_, 'from_stations_found, fromStation = true))
            .recover {
              case ex =>
                logger.error(s"stations api call failed", ex)
                TelegramMessages.MsgCommandFailed(cmd = cmd)
            }.pipeTo(clientChat)


        case (cmd@"/to") :: name :: _ =>
          stations.stationsByName(name)
            .map(mkStationsResponse(_, 'to_stations_found, fromStation = false))
            .recover {
              case ex =>
                logger.error(s"stations api call failed", ex)
                TelegramMessages.MsgCommandFailed(cmd = cmd)
            }.pipeTo(clientChat)

        case someWords =>
          logger.warn(s"unexpected words in TgUpdate.text: $someWords")
      }
  }

  private def mkStationsResponse
  (hits: Iterable[Station], templateId: Symbol, fromStation: Boolean): TelegramMessages.SendMsg = hits match {
    case coll if coll.isEmpty =>
      TelegramMessages.MsgSimple('stations_not_found)
    case coll =>
      val preparedStations = coll.map { station =>
        val encodedId = stationId.encode(id = station.id, fromStation)
        station.copy(id = encodedId)
      }

      TelegramMessages.MsgFoundStations(
        id = templateId,
        stations = preparedStations
      )
  }
}
