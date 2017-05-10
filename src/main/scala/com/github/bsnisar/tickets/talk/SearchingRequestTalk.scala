package com.github.bsnisar.tickets.talk

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.talk.TalkRoute.{ArrivalTimeCmd, DepartureTimeCmd, StationPointCmd}
import com.github.bsnisar.tickets.telegram.{MsgQueryUpdate, MsgWrongTimeFormat, Update}
import com.github.bsnisar.tickets.telegram.Update._
import com.typesafe.scalalogging.LazyLogging

import scala.util.Try
import scala.util.matching.Regex


object TalkRoute {
  private val ArrivalTime: Regex = "^/arrive\\s+(.*?)".r
  private val DepartureTime: Regex = "^/departure\\s+(.*?)".r

  sealed trait Cmd
  case class StationPointCmd(parsedId: Try[StationId.Id]) extends Cmd
  case class ArrivalTimeCmd(time: String) extends Cmd
  case class DepartureTimeCmd(time: String) extends Cmd
}

case class TalkRoute(ref: ActorRef, stationId: StationId) extends RouteLogic[Update] {
  override val specify: Routee = {
    case update @ Text(TalkRoute.ArrivalTime(timeStr)) =>
      UpdateEvent(update, ArrivalTimeCmd(timeStr))

    case update @ Text(TalkRoute.DepartureTime(timeStr)) =>
      UpdateEvent(update, DepartureTimeCmd(timeStr))

    case update @ Text(text) if stationId.decoder.isDefinedAt(text) =>
      UpdateEvent(update, StationPointCmd(stationId.decoder(text)))
  }
}

object SearchingRequestTalk {
  def props(chatID: String, telegram: ActorRef): Props = Props(classOf[SearchingRequestTalk], chatID, telegram)
}

class SearchingRequestTalk(val chatID: String, val notifyRef: ActorRef) extends Actor with LazyLogging {
  private var searchBean: SearchBean = Default()


  override def receive: Receive = {

    case UpdateEvent(update, Some(cmd@ArrivalTimeCmd(time))) =>
      logger.debug(s"on $cmd")
      try {
        val ldt = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE)
        searchBean.withArrive(ldt) match {
          case Req(reqBean) =>
            searchBean = reqBean
            runQuery(reqBean)

          case Modified(bean) =>
            searchBean = bean
            notifyRef ! update.mkReply(MsgQueryUpdate(searchBean))
        }
      } catch {
        case ex: DateTimeParseException =>
          logger.warn(s"wrong time format ArrivalTimeCmd($time)", ex)
          notifyRef ! update.mkReply(MsgWrongTimeFormat(time))
      }

  }


  private def runQuery(reqBean: SearchBean): Unit = ???


}
