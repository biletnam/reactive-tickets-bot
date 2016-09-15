package org.tikets.bot

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.tikets.bot.Stations.FindStationsReq
import org.tikets.misc.Cmd

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
