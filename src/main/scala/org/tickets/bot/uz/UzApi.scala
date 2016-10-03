package org.tickets.bot.uz

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Source}
import com.google.common.base.Supplier
import org.json4s.Reader
import org.tickets.misc.HttpSupport._

object UzApi {
  val RootPageHost = "booking.uz.gov.ua"
  val RootPage = s"http://$RootPageHost"
//  val FindStations = "/purchase/station/{stationNameFirstLetters}/"
  val FindTrains = "/purchase/search/"
  val GetCoaches = "/purchase/coaches/"
  val GetFreeSeats = "/purchase/coach/"

  /**
    * Build flow that transform request with specific API token.
    * @param token toke factory
    * @return flow of requests
    */
  def withTokenFlow(token: Supplier[String]): Flow[Request, Request, _] = Flow.fromFunction {
    case (httpReq, context) =>
      val headers = httpReq.headers :+ RawHeader("GV-Token", token.get())
      httpReq.withHeaders(headers) -> context
  }

  /**
    * UZ API connection pool
    * @param mt Materializer
    * @param as actor system
    * @return prepared flow
    */
  def http(implicit mt: Materializer, as: ActorSystem): Flow[Request, Response, _] =
    Http().newHostConnectionPool[Bound](RootPage)
}

/**
  * Station API model.
  *
  * @param id station id
  * @param name station name
  * @author bsnisar
  */
case class Station(id: String, name: String)

/**
  * Companion object for Station
  * @author bsnisar
  */
object Station {
  implicit object StationReader extends Reader[Station] {
    import org.json4s._
    import org.tickets.misc.HttpSupport.Json4sImplicits._

    def read(json: JValue): Station = Station(
      id = (json \ "station_id").extract[String],
      name = (json \ "title").extract[String]
    )
  }

}

case class Route(name: String)