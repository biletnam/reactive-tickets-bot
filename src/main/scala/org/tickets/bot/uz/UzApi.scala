package org.tickets.bot.uz

import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.scaladsl.Flow
import com.google.common.base.Supplier
import org.json4s.{JValue, Reader}
import org.tickets.misc.HttpSupport._

object UzApi {
  val RootPage = "http://booking.uz.gov.ua"
//  val FindStations = "/purchase/station/{stationNameFirstLetters}/"
  val FindTrains = "/purchase/search/"
  val GetCoaches = "/purchase/coaches/"
  val GetFreeSeats = "/purchase/coach/"

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

  final case class FetchRoutes(from: String, to: String)

  final case class RouteHits(routes: List[Route])
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
    import org.tickets.misc.HttpSupport.Json4sImplicits._
    import org.json4s._

    def read(json: JValue): Station = Station(
      id = (json \ "station_id").extract[String],
      name = (json \ "title").extract[String]
    )
  }

}

case class Route(name: String)