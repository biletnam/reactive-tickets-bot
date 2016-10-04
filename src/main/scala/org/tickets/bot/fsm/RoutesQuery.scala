package org.tickets.bot.fsm

import java.time.{LocalDate, LocalDateTime}

import akka.actor.{ActorRef, FSM}
import org.tickets.bot.fsm.RoutesQuery._
import org.tickets.bot.tg.{Telegram, TelegramRequests}
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand
import org.tickets.uz.cmd.FindStationsCommand.StationHits

/**
  * Dialog for routes definition.
  */
class RoutesQuery(val stationsApi: ActorRef) extends FSM[QueryState, Query] {
  private val telegram = context.actorSelection("/telegram")

  startWith(Idle, EmptyQuery)

  /**
    * Await income messages.
    */
  when(Idle) {
    case e @ Event(FindRoutes(getFrom, getTo), EmptyQuery) =>
      log.info("FindRoutes: {}", e)
      val req = Req(from = StationSearch(getFrom), to = StationSearch(getTo))
      stationsApi ! FindStationsCommand.FindStations(getFrom)
      goto(FromStationSearchReq) using req
    case Event(TelegramRequests.FindRouteTo(getTo), req) => ???
  }



  /**
    * Reply from StationsAPI for search req.
    */
  when(FromStationSearchReq) {
    case Event(StationHits(hits), req @ Req(StationSearch(_), _, _)) =>
      val matches = groupMatches(hits)
      goto(FromStationSearchAsk) using req.copy(from = StationSearchMatches(matches))
  }

  private def groupMatches(stations: List[Station]): Map[String, Station] = {
    val now: LocalDateTime = LocalDateTime.now()
    val seed: Int = now.getSecond + now.getMinute
    stations.zip(1 to stations.size).map{ case (st, idx) => (Integer.toHexString(seed + idx), st) }.toMap
  }

  /**
    * Await reply from client for stations pick up.
    */
  when(FromStationSearchAsk) {
    case Event(keyword: String, req @ Req(StationSearchMatches(variants), _, _))
      if variants.contains(keyword) =>
        val data = req.copy(from = StationDef(variants(keyword)))
        goto(DefQuery) using data
  }

  onTransition {
    case _ -> DefQuery =>
      stateData match {
        case Req(param, _, _) if !param.define =>
          log.info("param 1")
        case Req(_, param, _) if param.define =>
          goto(ToStationSearchReq)
        case _ => println("-----------!")
      }
  }

  private def actDestinationStation(req: Req): Unit = req match {
    case Req(_, EmptyStation, _) =>
      goto(ToStationSearchReq)

  }

  whenUnhandled {
    // common code for both states
    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  initialize()
}


object RoutesQuery {

  /**
    * State of query definition.
    */
  trait QueryState

  /**
    * Execute search.
    */
  case object DefQuery extends QueryState

  /**
    * Ready for income.
    */
  case object Idle extends QueryState

  /**
    * Ask API for station like this name.
    */
  case object FromStationSearchReq extends QueryState

  /**
    * Ask Client for selecting station.
    */
  case object FromStationSearchAsk extends QueryState

  /**
    * Ask API for station like this name.
    */
  case object ToStationSearchReq extends QueryState

  /**
    * Ask Client for selecting station.
    */
  case object ToStationSearchAsk extends QueryState

  /**
    * Query
    */
  trait Query {
    def empty: Boolean
  }

  /**
    * Empty query.
    */
  case object EmptyQuery extends Query {
    override def empty: Boolean = false
  }

  /**
    * Request
    * @param from from station
    * @param to to station
    * @param arrive arrive at
    */
  final case class Req(from: StationParam = EmptyStation,
                       to: StationParam = EmptyStation,
                       arrive: Option[LocalDate] = None) extends Query {
    override def empty: Boolean = (from == EmptyStation) && (to == EmptyStation) && arrive.isEmpty
  }


  /**
    * Station param.
    */
  trait StationParam {
    def define: Boolean = false
  }

  /**
    * Station param not set.
    */
  case object EmptyStation extends StationParam

  /**
    * Req to API for searching matching stations like name
    * @param name stations like
    */
  final case class StationSearch(name: String) extends StationParam

  /**
    * Stations that matches for given variants.
    * @param variants find variants
    */
  final case class StationSearchMatches(variants: Map[String, Station]) extends StationParam

  /**
    * Station define.
    * @param station station
    */
  final case class StationDef(station: Station) extends StationParam {
    override def define: Boolean = true
  }
}