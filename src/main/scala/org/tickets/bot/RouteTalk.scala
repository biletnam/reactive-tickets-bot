package org.tickets.bot

import akka.actor.{Actor, ActorRef, Status}
import akka.actor.Actor.Receive
import org.tickets.bot.RouteTalk.Hits
import org.tickets.misc.UniqueIndex
import org.tickets.railway.RailwayStations
import org.tickets.uz.Station

object RouteTalk {

  case class Hits(hits: Map[String, Station])

}

class RouteTalk(railwayStations: RailwayStations) extends Actor with UniqueIndex {
  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = ???


  def stenby(text: String): Unit = text.split(" ").toList match {
    case "/from" :: name :: Nil =>
      railwayStations.findStations(name)
        .map(groupBy(_)).map(Hits).pipeTo(self)
      context become waitForResults(name)
  }


  def waitForResults(name: String): Receive = {
    case Hits(hits) =>
    case Hits(hits) if hits.empty =>
    case Status.Failure(err) =>
  }


}
