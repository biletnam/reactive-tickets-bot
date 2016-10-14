package org.tickets.bot

import java.time.LocalDate

import akka.actor.{Actor, Props, Status}
import com.google.common.collect.Maps
import org.tickets.Station
import org.tickets.bot.RouteTalk._
import org.tickets.misc.{BundleKey, LogSlf4j, Text}
import org.tickets.railway.RailwayStations
import org.tickets.telegram.Update

object RouteTalk {

  def props(
             railwayStations: RailwayStations,
             notifier: TelegramNotification): Props = Props(classOf[RouteTalk], railwayStations, notifier)

  /**
    * Input message for Actor.
    * @param text test for input
    * @param up whole update [[Update]]
    */
  case class Cmd(text: String, up: Update) {
    def matches(patter: String) = text.startsWith(patter)
  }

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
      * Is query defined ?
      */
    def isDefined = from.isDefined && to.isDefined
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

  override def receive: Receive = command(Q())

  private def command(q: Q): Receive = {
    case Cmd(text, _) if text.startsWith("/start") =>
      notifier << "Hello this is a Bot!"
    case Cmd(text, _) if text.startsWith("/help") =>
      notifier << Text.bundle(BundleKey.ROUTES_HELP)
    case Cmd(text, _) if text.startsWith("/fst_") =>
      notifier << "not implemented"
    case Cmd(text, _) if text.startsWith("/tst_") =>
      notifier << "not implemented"
    case Cmd(text, _) if text.startsWith("/from") =>
      findStation(text.split(" ").toList, q)
    case Cmd(text, _) if text.startsWith("/to") =>
      findStation(text.split(" ").toList, q)
  }

  private def findStation(words: List[String], q: Q): Unit = words match {
    case cmd :: name :: Nil =>
      railwayStations.findStations(name)
        .map(groupStations).map(Hits).pipeTo(self)

      context become waitForResults(name, q)
  }

  private def waitForResults(name: String, q: Q): Receive = {
    case Hits(hits) if hits.nonEmpty =>
      val text = new Text().addBundle(BundleKey.STATIONS_LIST)

      for ((id, station) <- hits) {
        text.withDashes
          .addBundle(BundleKey.STATION_NAME, station.name)
          .addBundle(BundleKey.STATION_ID, id)
      }

      notifier push text.mkString
      context become command(q)

    case Hits(hits) if hits.isEmpty =>
      notifier << Text.bundle(BundleKey.STATION_SEARCH_ERR, name)
      context become command(q)

    case Status.Failure(err) =>
      log.error("station search failed", err)
      notifier << Text.bundle(BundleKey.STATION_SEARCH_ERR)
      context become command(q)
  }

  private def groupStations(stations: List[Station]) = stations
    .foldLeft(Map.empty[String, Station]) { (map, station) =>
      map + (station.identifier() -> station)
    }
}
