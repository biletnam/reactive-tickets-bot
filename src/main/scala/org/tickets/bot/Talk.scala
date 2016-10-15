package org.tickets.bot

import java.time.LocalDate

import akka.actor.{Props, Status}
import org.tickets.Station
import org.tickets.bot.Bot.Cmd
import org.tickets.bot.Talk._
import org.tickets.misc.{BundleKey, Text}
import org.tickets.railway.RailwayStations

object Talk {

  def props(railwayStations: RailwayStations, notifier: TelegramNotification): Props =
    Props(classOf[Talk], railwayStations, notifier)

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
class Talk(
     val railwayStations: RailwayStations,
     val notifier: TelegramNotification) extends Bot {

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

      this becomeOf waitForResults(name, q)
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
      this becomeOf command(q)

    case Hits(hits) if hits.isEmpty =>
      notifier << Text.bundle(BundleKey.STATION_SEARCH_ERR, name)
      this becomeOf command(q)

    case Status.Failure(err) =>
      log.error("station search failed", err)
      notifier << Text.bundle(BundleKey.STATION_SEARCH_ERR)
      this becomeOf command(q)
  }

  private def groupStations(stations: List[Station]) = stations
    .foldLeft(Map.empty[String, Station]) { (map, station) =>
      map + (station.identifier() -> station)
    }
}
