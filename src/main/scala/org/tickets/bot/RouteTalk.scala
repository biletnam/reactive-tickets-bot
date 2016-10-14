package org.tickets.bot

import java.time.LocalDate

import akka.actor.{Actor, Status}
import org.tickets.Station
import org.tickets.bot.RouteTalk._
import org.tickets.misc.LogSlf4j
import org.tickets.railway.RailwayStations
import org.tickets.telegram.{Update => Up}

object RouteTalk {

  type Control = PartialFunction[String, Any]

  /**
    * Station search hits.
    * @param hits map of id -> station
    */
  case class Hits(hits: Map[String, Station])

  /**
    * Notify that
    * @param q query
    */
  case class PartDone(q: Q)

  /**
    * Internal state as partially defined query.
    * @param from maybe from station
    * @param to maybe to station
    * @param arriveAt list af expected arrivals
    */
  final case class Q(from: Option[Station] = None, to: Option[Station] = None, arriveAt: List[LocalDate] = Nil) {

    /**
      * Produce list of remain commands.
      */
    def desc: String =
      s"""
        | Query: from[$from] to[$to]
        | Commands:
        |
      """.stripMargin

    /**
      * Is query defined ?
      */
    def isDefined = from.isDefined && to.isDefined
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

/**
  * Talk for defining particular route.
  * @param railwayStations service for stations search
  * @param notifier telegram notifier
  */
class RouteTalk(
     val railwayStations: RailwayStations,
     val notifier: TelegramNotification) extends Actor with LogSlf4j {

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
    case up: Up =>
      val cmdQuery: List[String] = up.text.split(" ").toList

      cmdQuery match {
        case "/from" :: name :: Nil =>
          log.debug("find 'from' station by {}", name)
          executeSearchRequest(name, new FromStation(q))

        case "/to" :: name :: Nil =>
          log.debug("find 'to' station by {}", name)
          executeSearchRequest(name, new ToStation(q))

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


  private def executeSearchRequest(name: String, lens: StationLens): Unit = {
    railwayStations.findStations(name)
      .map(groupStations).map(Hits).pipeTo(self)

    context become waitForResults(name, lens)
  }

  private def waitForResults(name: String, lens: StationLens): Receive = {
    case Hits(hits) =>
      val builder = new StringBuilder(
        TelegramNotification.Bundle.getString("list.of.stations")
      )

      for ((id, station) <- hits) {
        builder append s"\n -------------\n Station name: ${station.name}\n Identifier: /$id\n \n"
      }

      notifier push builder.mkString
      context become waitResponse(hits, lens)

    case Hits(hits) if hits.isEmpty =>
      notifier.pushCode(TelegramNotification.RailwayApiError, name)
      context become route(lens.q)

    case Status.Failure(err) =>
      log.error("station search failed", err)
      notifier pushCode TelegramNotification.RailwayApiError
      context become route(lens.q)
  }

  private def waitResponse(hits: Map[String, Station], from: StationLens): Receive = {
    case id: String if hits.contains(id) => ???
      context become route(from.withStation(hits(id)))
  }

  private def groupStations(stations: List[Station]): Map[String, Station] =
    stations.foldLeft(Map.empty[String, Station]) { (map, station) =>
      map + (station.identifier() -> station)
    }

}
