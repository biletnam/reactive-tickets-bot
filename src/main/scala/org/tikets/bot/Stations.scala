package org.tikets.bot

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.tikets.misc.Cmd

/**
  * Created by bsnisar on 14.09.16.
  */
object Stations {
  /**
    * Request for searching of station names by given pattern.
    * @param name pattern
    */
  final case class FetchStationNames(name: String, code: Any) extends Cmd

  /**
    * Result of names that was found.
    * @param matches matches.
    */
  final case class MatchStationNames(matches: List[Station], code: Any) extends Cmd


  case class Station(name: String, id: String)
}

class Stations extends Actor {
  override def receive: Receive = ???
}
