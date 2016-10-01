package org.tickets.bot.uz

import akka.actor.{Actor, ActorRef}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.OverflowStrategy
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.tickets.bot.uz.StationUz.FindStationsReq
import org.tickets.misc.HttpSupport._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

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




/*
class StationUz(val flow: Flow[Request, Response, _]) extends ActorPublisher[Request] {

  override def receive: Receive = {
    case FindStationsReq(name) =>
      val req = RequestBuilding.Get(s"/purchase/station/$name/") -> WithSender(sender())
      val ref: Source[Nothing, ActorRef] = Source.actorRef(1, OverflowStrategy.dropTail)
      val r = ref.via(flow).to(Sink.foreach {
        case (Success(httpResp), WithSender(aRef)) =>
        case (Failure(error), _) =>
        case _ =>
      })
  }
}
*/


