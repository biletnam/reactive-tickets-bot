package org.tickets.railway.uz

import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import com.google.common.base.Charsets
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s._
import org.json4s.JsonDSL._
import org.tickets.misc.{ApiProtocolException, LogSlf4j}
import org.tickets.misc.JsonSupport._
import org.tickets.railway.RailwayApi.Req

import scala.concurrent.ExecutionContext

object ApiRequestsUZ extends Json4sSupport  with LogSlf4j {

  val ApiDateTimeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  def createFindStationsByName(name: String): Req = {
    val encName = URLEncoder.encode(name, Charsets.UTF_8.name())
    val get: HttpRequest = RequestBuilding.Get(s"/ru/purchase/station/$encName/")
    get -> 101
  }

  def createFindTickets(fromId: String, toId: String, arrive: LocalDate, code: Any = 102)(implicit ex: ExecutionContext): Req = {
    val json: JValue =
      ("station_id_from" -> fromId) ~
      ("station_id_till" -> toId) ~
      ("date_dep" -> arrive.format(ApiDateTimeFormat))

    val get: HttpRequest = RequestBuilding.Post("/purchase/search/", json)
    get -> code
  }

  /**
    * Read UZ response and handle protocol response.
    * @param json body
    * @return payload json
    */
  def readContent(json: JValue): JValue = {
    val isError = (json \ "error").extract[Boolean]
    if (isError) {
      log.warn("error indicator is true, content {}", json)
      throw new ApiProtocolException("UZ api error indicator is true")
    }

    json \ "value"
  }

}
