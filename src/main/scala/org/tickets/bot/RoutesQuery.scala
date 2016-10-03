package org.tickets.bot

import java.time.LocalDate

import akka.actor.{ActorRef, FSM}
import org.tickets.bot.RoutesQuery._
import org.tickets.bot.uz.FindStationsCommand.StationHits
import org.tickets.bot.uz.{FindStationsCommand, Station}

/**
  * Dialog for routes definition.
  */
class RoutesQuery(val stationsApi: ActorRef, val telegram: ActorRef) extends FSM[QueryState, Query] {
  startWith(Idle, EmptyQuery)

  /**
    * Await income messages.
    */
  when(Idle) {
    case e @ Event(FindRoutes(getFrom, getTo), EmptyQuery) =>
      log.info("FindRoutes: {}", e)
      val req = Req(from = StationSearch(getFrom), to = StationSearch(getTo))
      stationsApi ! FindStationsCommand.withPattern(getFrom)
      goto(FromStationSearchReq) using req
    case _ => ???
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
    println(stations)
    Map()
  }

  /**
    * Await reply from client for stations pick up.
    */
  when(FromStationSearchAsk) {
    case Event(keyword: String, req @ Req(StationSearchMatches(variants), _, _))
      if variants.contains(keyword) =>
        val data = req.copy(from = StationDef(variants(keyword)))
        goto(DefQuery) using data
    case _ => log.warning("Ops"); stay()
  }


  when(DefQuery) {
    case _ => ???
  }


  onTransition {
    case _ -> DefQuery =>
      nextStateData match {
        case Req(param, _, _) if !param.define =>
          log.info("param 1")
        case Req(_, param, _) if param.define =>
          log.info("param 2")
        case _ => println("-----------!")
      }
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