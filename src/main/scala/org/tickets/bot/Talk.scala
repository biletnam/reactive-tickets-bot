package org.tickets.bot

import java.time.LocalDate

import akka.actor.{ActorRef, Props, Status}
import org.tickets.bot.Bot.Cmd
import org.tickets.bot.Talk._
import org.tickets.misc.{BundleKey, IdLikeCommand, PrefixedIdLike, Text}
import org.tickets.railway.RailwayStations
import org.tickets.railway.spy.Station
import org.tickets.railway.spy.Station.StationId
import org.tickets.telegram.Telegram.ChatId

import scala.util.{Failure, Success}

object Talk {

  def props(railwayStations: RailwayStations, notifier: TelegramNotification): Props =
    Props(classOf[Talk], railwayStations, notifier)

  class TalkProps(val railwayStations: RailwayStations, telegram: ActorRef) extends (ChatId => Props) {
    override def apply(id: ChatId): Props = props(railwayStations, NotifierRef(id, telegram))
  }


  /**
    * Station search hits.
    * @param hits map of id -> station
    */
  final case class Hits(hits: Map[String, Station])
  
  /**
    * Internal state as partially defined query.
    * @param from maybe from station
    * @param to maybe to station
    * @param arriveAt list af expected arrivals
    */
  final case class Q(from: Option[StationId] = None, 
                     to: Option[StationId] = None, 
                     arriveAt: List[LocalDate] = List.empty) {

    /**
      * Is query defined ?
      */
    def isDefined = from.isDefined && to.isDefined
  }
  
  val StationFromCommandPrefix = "/fst_"
  val StationToCommandPrefix = "/tst_"

  val FromCommandIdCodec = new PrefixedIdLike[String](StationFromCommandPrefix)
  val ToCommandIdCode = new PrefixedIdLike[String](StationToCommandPrefix)
}

/**
  * Conversation with client.
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
      notifier << BundleKey.ROUTES_HELP.getText

    case Cmd(text, _) if text.startsWith("/help") =>
      notifier << BundleKey.ROUTES_HELP.getText


    case Cmd(text, _) if text.startsWith("/from") =>
      findStation(text.split(" ").toList, q) (FromCommandIdCodec)
    case Cmd(text, _) if text.startsWith("/to") =>
      findStation(text.split(" ").toList, q)(ToCommandIdCode)


    case Cmd(text, _) if text.startsWith(StationFromCommandPrefix) =>
      val id = FromCommandIdCodec.decode(text)
      railwayStations.station(id).onComplete {
        case Success(station) =>
          notifier << s"Route from ${station.name}"
        case Failure(err) =>
          log.error("can't find station (cmd-id={}, id={})", text, id, err)
      }
      this becomeOf command(q.copy(from = Some(id)))

    case Cmd(text, _) if text.startsWith("/tst_") =>
      this becomeOf command(q.copy(to = Some(text)))
      notifier << "not implemented"
  }

  private def findStation(words: List[String], q: Q)(implicit id: IdLikeCommand[String]): Unit = words match {
    case cmd :: name :: Nil =>
      railwayStations.findStations(name)
        .map(groupStations).map(Hits).pipeTo(self)
      this becomeOf waitForResults(name, q)
      
    case cmd :: Nil =>
      notifier <<  BundleKey.SECOND_ARGUMENT_REQUIRED.getText
      this becomeOf command(q)

    case _ =>
      notifier <<  BundleKey.UNKNOWN_COMMAND.getText
      this becomeOf command(q)
  }

  private def waitForResults(name: String, q: Q): Receive = {
    case Hits(hits) if hits.nonEmpty =>
      val text = new Text()
        .addLine(BundleKey.STATIONS_FOUND_LIST.getTemplateText(name))

      for ((id, station) <- hits) {
        text.withDashes
          .addLine(BundleKey.STATION_NAME.getTemplateText(station.name))
          .addLine(BundleKey.STATION_ID.getTemplateText(id))
      }

      notifier push text.mkString
      this becomeOf command(q)

    case Hits(hits) if hits.isEmpty =>
      notifier << BundleKey.STATION_SEARCH_ERR.getTemplateText(name)
      this becomeOf command(q)

    case Status.Failure(err) =>
      log.error("station search failed", err)
      notifier << BundleKey.STATION_SEARCH_ERR.getText
      this becomeOf command(q)
  }
  
  private def groupStations(stations: List[Station])(implicit id: IdLikeCommand[String]) = stations
    .foldLeft(Map.empty[String, Station]) { (map, station) =>
      map + (id.encode(station.identifier) -> station)
    }
}
