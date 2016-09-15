package org.tikets.bot

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by bsnisar on 15.09.16.
  */
class Stations extends Actor {
  override def receive: Receive = ???
}

object Stations {

  case class Station(id: String)

  case class FindStationsLike(name: String)

  case class StationHits(options: List[Station])

}