package org.tickets.bot.uz

import akka.NotUsed
import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.google.common.base.Supplier
import org.tickets.bot.uz.StationUz.FindStationsReq
import org.tickets.bot.uz.UzApi.FetchStations
import org.tickets.misc.HttpSupport.{Bound, Command, Request, Response}
import org.tickets.misc.Log

import scala.collection.immutable.Seq
import scala.util.{Failure, Success}

/**
  * Created by bsnisar on 01.10.16.
  */
class UzApi extends ActorPublisher[Request] with Log {
  val maxRetry = 4

  override def receive: Receive = {
    case cmd: Command[FetchStations]=>
      if (stopRetry(cmd))
        log.warn("receive#'Max resending attempts is reached' {}", cmd)
      else {
        val station: Request = UzApi.findStation(cmd.withNext)
        log.debug("receive#onNext({})", cmd)
        onNext(station)
      }
  }


  private def stopRetry(cmd: Command[FetchStations]): Boolean
    = cmd.seq + 1 > maxRetry
}


object UzApi {
  val RootPage = "http://booking.uz.gov.ua"
  val FindStations = "/purchase/station/{stationNameFirstLetters}/"
  val FindTrains = "/purchase/search/"
  val GetCoaches = "/purchase/coaches/"
  val GetFreeSeats = "/purchase/coach/"

  /**
    * Create find stations command.
    * @return
    */
  def findStation(command: Command[FetchStations]): Request = {
    val req = RequestBuilding.Get(s"/purchase/station/${command.payload.like}/")
    req -> command
  }

  /**
    * Build flow that transform request with specific API token.
    * @param token toke factory
    * @return flow of requests
    */
  def withToken(token: Supplier[String]): Flow[Request, Request, _] = Flow.fromFunction {
    case (httpReq, context) =>
      val headers = httpReq.headers :+ RawHeader("GV-Token", token.get())
      httpReq.withHeaders(headers) -> context
  }

  /**
    * Ask API for stations.
    * @param like like name
    */
  final case class FetchStations(like: String)

  /**
    * Found stations in API
    * @param stations stations
    */
  final case class StationHits(stations: List[Station])

  final case class FetchRoutes(from: String, to: String)

  final case class RouteHits(routes: List[Route])
}

case class Station(id: String, name: String)

case class Route(name: String)