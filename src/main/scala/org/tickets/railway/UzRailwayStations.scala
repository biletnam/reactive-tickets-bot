package org.tickets.railway
import java.util.concurrent.TimeoutException

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.common.cache.{Cache, CacheBuilder}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JsonAST.JArray
import org.json4s._
import org.tickets.Station
import org.tickets.Station.StationId
import org.tickets.misc.{ApiProtocolException, HttpProtocolException, LogSlf4j}
import org.tickets.railway.Api.ApiFlow

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * Search throw UZ railway API
  */
class UzRailwayStations(val httpFlow: ApiFlow)(implicit ec: ExecutionContext, mt: Materializer)
extends RailwayStations with LogSlf4j with Json4sSupport {

  final type StationsResp = Future[List[Station]]

  private lazy val cash: Cache[StationId, Station] =
    CacheBuilder
      .newBuilder()
      .build[StationId, Station]()

  import org.tickets.misc.JsonSupport._

  override def findStations(byName: String): StationsResp = {
    log.debug("[#findStations] perform search '{}'", byName)
    Source.single(UzRailwayApiRequests.findStationByNameReq(byName))
      .via(httpFlow)
      .runWith(Sink.head)
        .flatMap {
          case (tryResponse, _) => handle(tryResponse)
        }
  }

  override def station(id: StationId): Future[Station] = {
    Future.successful(cash.getIfPresent(id))
  }

  private def handle(response: Try[HttpResponse]): StationsResp = response match {
    case Success(httpResponse) if httpResponse.status.isSuccess() =>
      val stations = Unmarshal(httpResponse.entity).to[JValue].map(parseJson)
      stations
    case Success(httpResponse) if !httpResponse.status.isSuccess() =>
      log.warn("api respond by not success status {}", httpResponse.status.value)
      Future.failed(new HttpProtocolException(httpResponse.status))
    case Failure(err) =>
      log.error("request send failed", err)
      Future.failed(err)
  }

  private def parseJson(json: JValue): List[Station] = {
    val isError = (json \ "error").extract[Boolean]
    if (isError) {
      log.warn("error indicator is true, content {}", json)
      throw new ApiProtocolException("UZ api error indicator is true")
    }

    json \ "value" match {
      case JArray(stations) =>
        val content = stations.foldLeft(List.empty[Station]) { (list, data) =>
          import Station._
          val station = data.as[Station]
          cash.put(station.identifier, station)
          station :: list
        }
        log.trace("[#parseJson] found content {}", content)
        content
      case e @ _ =>
        log.error("[UzApi] Unknown json structure. Expect {'value': [...]}, but was {}", e)
        throw new ApiProtocolException("[UzApi] unknown json structure")
    }
  }
}
