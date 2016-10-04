package org.tickets.bot

import org.tickets.uz.Station


object BotMessages {

}

/**
  * Client request for defining route departure.
  * @param name name like
  */
case class ReqRouteDeparture(name: String)

/**
  * Bot question for picking up stations
  * @param variants station variants
  */
case class AskRouteDepartures(variants: Map[String, Station])

/**
  * Client selection for stations
  * @param variantId variant id
  */
case class RouteDeparturePicked(variantId: String)

/**
  * Client request for defining route arrival.
  * @param name name like
  */
case class ReqRouteArrival(name: String)