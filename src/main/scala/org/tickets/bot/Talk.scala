package org.tickets.bot

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.{ActorRef, Props, Status}
import com.softwaremill.quicklens
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
  final case class Session(from: Option[StationId] = None,
                           to: Option[StationId] = None,
                           arriveAt: List[LocalDate] = List.empty) {

    /**
      * Is query defined ?
      */
    def isDefined = from.isDefined && to.isDefined
  }

  import com.softwaremill.quicklens._

  type ModifySession = (Talk.Session) => quicklens.PathModify[Talk.Session, Option[Station.StationId]]

  val ModifyFromSession: ModifySession = modify(_: Session)(_.from)
  val ModifyToSession: ModifySession = modify(_: Session)(_.to)

  /**
    * Prefix for commands that match to departure station ids
    */
  val StationFromCommandPrefix = "/fst_"

  /**
    * Prefix for commands that match to arrival station ids
    */
  val StationToCommandPrefix = "/tst_"

  /**
    * Prefixed that apply specific pattern for departure station ids
    */
  val FromCommandIdCodec = new PrefixedIdLike[String](StationFromCommandPrefix)

  /**
    * Prefixed that apply specific pattern for arrival station ids
    */
  val ToCommandIdCode = new PrefixedIdLike[String](StationToCommandPrefix)


  val ArgTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yy")
  val DisplayTimeFormat = DateTimeFormatter.ofPattern("dd LLL yyyy")
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

  override def receive: Receive = command(Session())

  private def command(q: Session): Receive = {
    case Cmd(text, _) if text.startsWith("/start") =>
      notifier << BundleKey.ROUTES_HELP.getText

    case Cmd(text, _) if text.startsWith("/help") =>
      notifier << BundleKey.ROUTES_HELP.getText

    case Cmd(text, _) if text.startsWith("/from") =>
      val args: List[String] = text.split(" ").toList
      findStation(args, q) (FromCommandIdCodec)

    case Cmd(text, _) if text.startsWith("/to") =>
      val args: List[String] = text.split(" ").toList
      findStation(args, q)(ToCommandIdCode)

    case Cmd(text, _) if text.startsWith("/arriveTo") =>
      val args = text.split(" ").toList
      specifyArrivalTime(args, q)

    case Cmd(text, _) if text.startsWith(StationFromCommandPrefix) =>
      val id = FromCommandIdCodec.decode(text)
      railwayStations.station(id).pipeTo(self)
      this becomeOf waitStationDetails(q, ModifyFromSession)

    case Cmd(text, _) if text.startsWith(StationToCommandPrefix) =>
      val id = ToCommandIdCode.decode(text)
      railwayStations.station(id).pipeTo(self)
      this becomeOf waitStationDetails(q, ModifyToSession)
  }

  private def specifyArrivalTime(args: List[String], session: Session): Unit =
    forSecondArg(args, session) { times =>
      val dates = times.split(",")
        .view.map(_.trim).map(LocalDate.parse(_, ArgTimeFormat)).toList

      notifier << BundleKey.ARRIVAL_TIME_DEFINED.getTemplateText(
        dates.map(_.format(DisplayTimeFormat)).mkString(", "), session
      )

      this becomeOf command(session.copy(arriveAt = dates))
    }

  /**
    * Parse several arguments: expect 'command' 'word-for-search'.
    * Call railway service for getting stations. Prepare response and generate with map
    * of found stations where key is generated id-lice command, that can be selected by used.
    *
    * @param words input words
    * @param q query
    * @param id id command codec
    */
  private def findStation(words: List[String], q: Session)
                         (implicit id: IdLikeCommand[String]): Unit =
  forSecondArg(words, q) { name =>
    railwayStations.findStations(name)
      .map(groupStations).map(Hits).pipeTo(self)

    this becomeOf waitForResults(name, q)
  }

  /**
    * Evaluate function for expected second argument for command.
    * Handle common behavior for other types of input.
    *
    * @param args all args
    * @param q session
    * @param onArgs callback
    */
  private def forSecondArg(args: List[String], q: Session)(onArgs: String => Unit): Unit = args match {
    case cmd :: name :: Nil =>
      onArgs(name)

    case cmd :: Nil =>
      notifier <<  BundleKey.SECOND_ARGUMENT_REQUIRED.getText
      this becomeOf command(q)

    case _ =>
      notifier <<  BundleKey.UNKNOWN_COMMAND.getText
      this becomeOf command(q)
  }

  /**
    * Wait for response from railway service. Use timeout for search.
    * @param name name for search
    * @param q query
    */
  private def waitForResults(name: String, q: Session): Receive = {
    case Hits(hits) if hits.nonEmpty =>
      val text = new Text().addLine(BundleKey.STATIONS_FOUND_LIST.getTemplateText(name))

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

  private def waitStationDetails(q: Session, modify: ModifySession): Receive = {
    case station: Station =>
      val newSession = modify(q).setTo(Option(station.identifier))
      notifier << BundleKey.STATION_DEFINED.getTemplateText(station.name, q)
      this becomeOf command(newSession)

    case Status.Failure(err) =>
      notifier << BundleKey.STATION_SEARCH_ERR.getText
      this becomeOf command(q)
  }

  private def groupStations(stations: List[Station])(implicit id: IdLikeCommand[String]) = stations
    .foldLeft(Map.empty[String, Station]) { (map, station) =>
      map + (id.encode(station.identifier) -> station)
    }
}
