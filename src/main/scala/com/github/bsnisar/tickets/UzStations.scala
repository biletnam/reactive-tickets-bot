package com.github.bsnisar.tickets

import java.net.URLEncoder

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.WsFlow
import com.github.bsnisar.tickets.misc.Json
import com.google.common.base.Charsets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class UzStations(val http: WsFlow)(implicit mt: Materializer) extends Stations with Json {

  override def stationsByName(name: String): Future[Seq[Station]] = {
    val encName = URLEncoder.encode(name, Charsets.UTF_8.name())
    val req: HttpRequest = RequestBuilding.Get(s"/ru/purchase/station/$encName/")

    val stations: Future[Seq[Station]] = Source.single(req -> name)
      .via(http)
      .runWith(Sink.head)
      .map(json => json.extract[List[Station]])


    stations
  }

}

