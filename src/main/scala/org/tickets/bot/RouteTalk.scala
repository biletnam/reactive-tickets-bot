package org.tickets.bot

import akka.actor.{Actor, ActorRef, Status}
import org.tickets.telegram.{TelegramPush, Update => Up}
import org.tickets.bot.RouteTalk._
import org.tickets.misc.{LogSlf4j, UniqueIndex}
import org.tickets.railway.RailwayStations
import org.tickets.telegram.Telegram.ChatId
import org.tickets.uz.Station

object RouteTalk {

  type Control = PartialFunction[String, Any]

  /**
    * Station search hits
    * @param hits map of id -> station
    */
  case class Hits(hits: Map[String, Station])

  case class PartDone(q: Q)

  case class Q(from: Option[Station] = None, to: Option[Station] = None) {

    def desc: String =
      s"""
        | Query: from[$from] to[$to]
        | Commands:
        |
      """.stripMargin


    def isDefined = false
  }

  trait StationLens {
    def q: Q
    def withStation(st: Station): Q
  }

  class FromStation(val q: Q) extends StationLens {
    override def withStation(st: Station): Q = q.copy(from = Some(st))
  }

  class ToStation(val q: Q) extends StationLens {
    override def withStation(st: Station): Q = q.copy(to = Some(st))
  }
}

class RouteTalk(railwayStations: RailwayStations, notifier: TelegramNotification) extends Actor with UniqueIndex with LogSlf4j {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = generalControl

  private def generalControl: Receive = {
    case up: Up if matches(up, "/cancel") => ???
      notifier << ""
    case up: Up if matches(up, "/route") =>
      context become route(Q())
  }

  private def matches(up: Up, ptrn: String): Boolean = {
    up.text.startsWith(ptrn)
  }

  private def route(q: Q): Receive = {
    case up: Up => up.text.split(" ").toList match {
        case "/from" :: name :: Nil =>
          log.debug("find 'from' station by {}", name)
          searchForDeparture(q, name)

        case "/to" :: name :: Nil =>
          log.debug("find 'to' station by {}", name)
          searchForArrival(q, name)

        case "/arriveAt" :: time :: Nil =>
          log.debug("specify arrive time {}", time)

        case e @ _ =>
          notifier << "command unknown"
    }

    case PartDone =>
      if (q.isDefined) {
        context.parent ! q
      }

    case e @ _ =>
      log.warn("unexpected message {}", e)
  }


  private def searchForArrival(q: Q, name: String): Unit = {
    railwayStations.findStations(name)
      .map(groupBy(_)).map(Hits).pipeTo(self)

    context become waitForResults(name, new ToStation(q))
  }

  private def searchForDeparture(q: Q, name: String): Unit = {
    railwayStations.findStations(name)
      .map(groupBy(_)).map(Hits).pipeTo(self)

    context become waitForResults(name, new FromStation(q))
  }

  private def waitForResults(name: String, lens: StationLens): Receive = {
    case Hits(hits) =>

    case Hits(hits) if hits.empty =>

    case Status.Failure(err) =>
      log.error("station search failed", err)
      context become route(lens.q)
  }

  private def waitResponse(hits: Map[String, Station], from: StationLens): Receive = {
    case id: String if hits.contains(id) => ???
      context become route(from.withStation(hits(id)))
  }

}
