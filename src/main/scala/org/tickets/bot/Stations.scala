package org.tickets.bot

import akka.actor.Actor
import org.tickets.bot.Stations.FindStationsReq

class Stations extends Actor {

  override def receive: Receive = {
    case FindStationsReq(name) => println(name)
  }
}

/**
  * Stations partial object.
  *
  * @author bsnsiar
  */
object Stations {
  /**
    * Request for searching of station names by given pattern.
    * @param name pattern
    */
  final case class FindStationsReq(name: String)

  /**
    * Result of names that was found.
    * @param matches matches.
    */
  final case class StationHits(matches: List[Station])

  /**
    * Station reply.
    * @param name station name
    * @param id station id
    */
  case class Station(name: String, id: String)
}
