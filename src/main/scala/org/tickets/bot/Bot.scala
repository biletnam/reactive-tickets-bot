package org.tickets.bot

import org.tickets.uz.Station

object Bot {

  /**
    * Client request for specifying station departure
    * @param name name
    */
  case class ReqDepartureStation(name: String)

  /**
    * Ask client for pick up station from.
    * @param variants variants
    */
  case class AskDepartureStationsFrom(variants: Map[String, Station])

  case class ReplyPickStation(id: String)
}
