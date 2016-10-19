package org.tickets.bot

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.{Actor, ActorRef, Props, Status}
import com.softwaremill.quicklens
import org.tickets.bot.Bot.Cmd
import org.tickets.bot.DefineRouteTalk._
import org.tickets.misc.{BundleKey, LogSlf4j, Text}
import org.tickets.railway.RailwayStations
import org.tickets.railway.model.Station
import org.tickets.telegram.TelegramApi.ChatId

object DefineRouteTalk {

  def props(railwayStations: RailwayStations, notifier: Notifier): Props =
    Props(classOf[DefineRouteTalk], railwayStations, notifier)

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
  final case class Session(from: Option[Station] = None,
                           to: Option[Station] = None,
                           arriveAt: List[LocalDate] = List.empty) {

    /**
      * Is query defined ?
      */
    def isDefined = from.isDefined && to.isDefined && arriveAt.nonEmpty
  }

  /**
    * Format for argument
    */
  val ArgTimeFormat = DateTimeFormatter.ofPattern("dd-MM-yy")

  /**
    * Display time format.
    */
  val DisplayTimeFormat = DateTimeFormatter.ofPattern("dd LLL yyyy")

  import com.softwaremill.quicklens._

  type ModifySession = (DefineRouteTalk.Session) => quicklens.PathModify[DefineRouteTalk.Session, Option[Station]]
  val ModifyFromSession: ModifySession = modify(_: Session)(_.from)
  val ModifyToSession: ModifySession = modify(_: Session)(_.to)
}

/**
  * Conversation with client.
  * @param railwayStations service for stations search
  * @param notifier telegram notifier
  */
class DefineRouteTalk(
     val railwayStations: RailwayStations,
     val notifier: Notifier) extends Actor with LogSlf4j {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = idle(Session())

  private def newIdleCommand(q: Session): Receive = {
    if (q.isDefined) {
      //todo: search for tickets
      idle(q)
    } else {
      idle(q)
    }
  }

  /**
    * Idle for input and react on it.
    * @param q route query session
    */
  private def idle(q: Session): Receive = {
    case Cmd(text, _) if text.startsWith("/start") =>
      notifier << BundleKey.ROUTES_HELP.getText

    case Cmd(text, _) if text.startsWith("/help") =>
      notifier << BundleKey.ROUTES_HELP.getText

    case Cmd(text, _) if text.startsWith("/from") =>
      val args: List[String] = text.split(" ").toList
      executeStationSearch(args, q) (ModifyFromSession)

    case Cmd(text, _) if text.startsWith("/to") =>
      val args: List[String] = text.split(" ").toList
      executeStationSearch(args, q)(ModifyToSession)

    case Cmd(text, _) if text.startsWith("/arriveTo") =>
      val args = text.split(" ").toList
      specifyArrivalTime(args, q)
  }

  private def specifyArrivalTime(args: List[String], session: Session): Unit =
    forSecondArg(args, session) { times =>
      val dates = times.split(",")
        .view.map(_.trim).map(LocalDate.parse(_, ArgTimeFormat)).toList

      notifier << BundleKey.ARRIVAL_TIME_DEFINED.getTemplateText(
        dates.map(_.format(DisplayTimeFormat)).mkString(", "), session
      )

      context become newIdleCommand(session.copy(arriveAt = dates))
    }

  /**
    * Parse several arguments: expect 'command' 'word-for-search'.
    * Call railway service for getting stations. Prepare response and generate with map
    * of found stations where key is generated id-lice command, that can be selected by used.
    *
    * @param words input words
    * @param q query
    */
  private def executeStationSearch(words: List[String], q: Session)
                         (implicit setter: ModifySession): Unit =
  forSecondArg(words, q) { name =>
    railwayStations.findStations(name)
      .map(groupStations).map(Hits).pipeTo(self)

    context become waitForResults(name, q)
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
      context become idle(q)

    case _ =>
      notifier <<  BundleKey.UNKNOWN_COMMAND.getText
      context become idle(q)
  }

  /**
    * Wait for response from railway service. Use timeout for search.
    * @param name name for search
    * @param q query
    */
  private def waitForResults(name: String, q: Session)
                            (implicit modifySession: ModifySession): Receive = {
    case Hits(hits) if hits.nonEmpty =>
      val text = new Text()
        .addLine(BundleKey.STATIONS_FOUND_LIST.getTemplateText(name))
        .withDashes
      for ((id, station) <- hits) {
        text
          .addLine(BundleKey.STATION_NAME.getTemplateText(station.name))
          .addLine(BundleKey.STATION_ID.getTemplateText(id))
      }

      notifier push text.mkString
      context become waitSelection(hits, q)

    case Hits(hits) if hits.isEmpty =>
      notifier << BundleKey.STATION_SEARCH_ERR.getTemplateText(name)
      context become idle(q)

    case Status.Failure(err) =>
      log.error("station search failed", err)
      notifier << BundleKey.STATION_SEARCH_ERR.getText
      context become idle(q)
  }

  private def waitSelection(hits: Map[String, Station], q: Session)(implicit lens: ModifySession): Receive = {
    case Cmd(id, _) if hits.contains(id) =>
      val station = hits(id)
      val newSession: Session = lens.apply(q).setTo(Some(station))
      notifier << BundleKey.STATION_DEFINED.getTemplateText(station.name, newSession)
      context become newIdleCommand(newSession)

    case Cmd(id, _) if !hits.contains(id) =>
      notifier << "miss"

    case Cmd(text, _) if text.startsWith("/find") =>
      val args: List[String] = text.split(" ").toList
      executeStationSearch(args, q)(lens)
  }

  private def groupStations(stations: List[Station]) = stations
    .foldLeft(Map.empty[String, Station]) { (map, station) =>
      map + (s"/st_${station.uid}" -> station)
    }
}
