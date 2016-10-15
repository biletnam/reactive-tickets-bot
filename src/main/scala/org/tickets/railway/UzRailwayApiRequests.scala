package org.tickets.railway

import java.net.URLEncoder

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import com.google.common.base.Charsets
import org.tickets.railway.Api.Req

object UzRailwayApiRequests {

  def findStationByNameReq(name: String): Req = {
    val encName = URLEncoder.encode(name, Charsets.UTF_8.name())
    val get: HttpRequest = RequestBuilding.Get(s"/ru/purchase/station/$encName/")
    get -> 101
  }

}
