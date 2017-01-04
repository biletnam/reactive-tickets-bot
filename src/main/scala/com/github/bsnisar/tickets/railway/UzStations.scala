package com.github.bsnisar.tickets.railway
import java.net.URLEncoder

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.{Sink, Source}
import Ws.HttpFlow
import com.github.bsnisar.tickets.Ws
import com.google.common.base.Charsets
import org.json4s.JValue

import scala.concurrent.Future



class UzStations(val http: HttpFlow) extends Stations {

  override def stationsByName(name: String): Future[Seq[Station]] = {
    val encName = URLEncoder.encode(name, Charsets.UTF_8.name())
    val req: HttpRequest = RequestBuilding.Get(s"/ru/purchase/station/$encName/")

    val stations: Future[JValue] = Source.single(req -> name)
      .via(http)
      .mapAsync(1)(Ws.asJSON)
      .runWith(Sink.head)

    ???
  }

}
