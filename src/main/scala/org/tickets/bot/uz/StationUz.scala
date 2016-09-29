package org.tickets.bot.uz

import akka.actor.Actor
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.javadsl.{Sink, Source}
import akka.stream.scaladsl.Flow
import org.tickets.bot.uz.StationUz.FindStationsReq

import scala.util.Try

/**
  * Stations partial object.
  * @author bsnsiar
  */
object StationUz {
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

/**
  * UZ service.
  * @param flow http flow
  */
class StationUz(val flow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), _]) extends Actor {

  override def receive: Receive = {
    case FindStationsReq(name) =>
      println(name)
  }
}


