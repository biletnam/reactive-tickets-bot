package org.tickets.railway

import java.net.URLEncoder

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.google.common.base.Charsets
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s._
import org.tickets.Station
import org.tickets.misc.{ApiProtocolException, HttpProtocolException, LogSlf4j}
import org.tickets.railway.Api.{Command, Req}

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success, Try}

object UzFindStationsReq {

  def request(name: String, promise: Promise[List[Station]]): Req = {
    val encName = URLEncoder.encode(name, Charsets.UTF_8.name())
    val get: HttpRequest = RequestBuilding.Get(s"/ru/purchase/station/$encName/")
    get -> new UzFindStationsReq(promise)
  }

}

case class UzFindStationsReq(promise: Promise[List[Station]]) extends Command with LogSlf4j with Json4sSupport {
  import org.tickets.misc.JsonUtil._

  def run(response: Try[HttpResponse])
         (implicit mt: Materializer, ec: ExecutionContext): Unit = response match {

    case Success(httpResponse) if httpResponse.status.isSuccess() =>
      val stations = Unmarshal(httpResponse.entity).to[JValue].map(parseJson)
      stations onComplete { promise complete }
    case Success(httpResponse) if !httpResponse.status.isSuccess() =>
      log.warn("api respond by not success status {}", httpResponse.status.value)
      promise.failure(new HttpProtocolException(httpResponse.status))
    case Failure(err) =>
      log.error("request send failed", err)
      promise.failure(err)
  }

  private def parseJson(json: JValue): List[Station] = {
    val isError = (json \ "error").extract[Boolean]
    if (isError) {
      log.warn("error indicator is true, content {}", json)
      throw new ApiProtocolException("uz api error indocator is true")
    }

    json \ "value" match {
      case JArray(stations) =>
        val content = stations.foldLeft(List.empty[Station]) { (list, data) =>
          import Station._
          val station = data.as[Station]
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
