package com.github.bsnisar.tickets.talk

import java.util.Base64

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.Stations
import com.github.bsnisar.tickets.telegram.{TelegramMessages, TgUpdate}
import com.google.common.base.Charsets
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future


object Talk {

  def props(stations: Stations, tg: ActorRef): Props = Props(classOf[Talk], stations, tg)
}

class Talk(val stations: Stations, val telegram: ActorRef) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case update: TgUpdate =>
      logger.debug(s"try understand ( ${update.text} )")
      understand(update.text.split(" ").toList)

    case msg: TelegramMessages.SendMsg =>
      logger.debug(s"sending telegram message $msg")
      telegram ! msg

    case e =>
      logger.warn(s"unknown message $e")
  }


  private def understand(words: List[String]): Unit = words match {
    case (cmd @ "/from") :: name :: _ =>
      mkStationsMessage(
        searchByName = name,
        templateId = 'from_station,
        encodePrefix = "f::"
      ).recover {
        case ex =>
          logger.error(s"stations api call failed", ex)
          TelegramMessages.MsgCommandFailed(cmd = cmd)
      }.pipeTo(self)


    case (cmd @ "/to") :: name :: _ =>
      mkStationsMessage(
        searchByName = name,
        templateId = 'to_station,
        encodePrefix = "t::"
      ).recover {
        case ex =>
          logger.error(s"stations api call failed", ex)
          TelegramMessages.MsgCommandFailed(cmd = cmd)
      }.pipeTo(self)

    case prefix :: _ if prefix.startsWith("/goto_") =>

    case "/hello" :: _ =>
  }


  private def mkStationsMessage(searchByName: String,
                                templateId: Symbol,
                                encodePrefix: String): Future[TelegramMessages.SendMsg] = {
    stations.stationsByName(searchByName).map {
      case hits if hits.isEmpty =>
        TelegramMessages.MsgSimple('station_not_found)
      case hits  =>
        TelegramMessages.MsgFoundStations(
          id = templateId,
          stations = hits.map(station => station.copy(id = encode(encodePrefix, station.id)))
        )
    }
  }

  private def encode(prefix: String, id: String): String = {
    val msg = s"$prefix$id"
    Base64.getEncoder.encodeToString(msg.getBytes(Charsets.UTF_8))
  }

}
