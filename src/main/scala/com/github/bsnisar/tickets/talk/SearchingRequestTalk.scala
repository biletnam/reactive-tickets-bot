package com.github.bsnisar.tickets.talk

import java.time.{LocalDate, LocalDateTime}
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

  /**
    * ID of a station.
    * @param parsedId parsed id
    * @see [[StationId]]
    */
  case class StationPointCmd(parsedId: Try[StationId.Id]) extends Cmd

  /**
    * Command for arrival time.
    * @param time time in YYYY-MM-DD
    */
  case class ArrivalTimeCmd(time: String) extends Cmd

  /**
    * Command for departure time.
    * @param time YYYY-MM-DD
    */
  case class DepartureTimeCmd(time: String) extends Cmd
}

/**
  * Route logic for current actor.
  * @param ref destination actor
  * @param stationId station id parser
  */
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
  val CmdTimeFormat: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
}

class SearchingRequestTalk(val chatID: String, val notifyRef: ActorRef) extends Actor with LazyLogging {
  private var searchBean: SearchBean = Default()


  override def receive: Receive = {
    case e@UpdateEvent(update, Some(ArrivalTimeCmd(time))) =>
      logger.debug(s"receive update event $e")
      modifyTimeSlot(update, time, dateTime => searchBean.withArrive(dateTime))

    case e@UpdateEvent(update, Some(DepartureTimeCmd(time))) =>
      logger.debug(s"receive update event $e")
      modifyTimeSlot(update, time, dateTime => searchBean.withDeparture(dateTime))
  }

  private def modifyTimeSlot(update: Update, time: String, f: LocalDate => SearchBeanUpdate): Unit = {
    try {
      val dateTime = LocalDate.parse(time, SearchingRequestTalk.CmdTimeFormat)
      f(dateTime) match {
        case Req(bean) =>
          searchBean = bean
          runQuery(bean)

        case Modified(bean) =>
          searchBean = bean
          notifyRef ! update.createAnswer(MsgQueryUpdate(searchBean))
      }
    } catch {
      case ex: DateTimeParseException =>
        logger.warn(s"wrong time format $time, update: $update", ex)
        notifyRef ! update.createAnswer(MsgWrongTimeFormat(time))
    }
  }

  private def runQuery(reqBean: SearchBean): Unit = ???


}
