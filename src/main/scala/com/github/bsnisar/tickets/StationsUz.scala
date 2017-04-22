package com.github.bsnisar.tickets

import java.net.URLEncoder
import java.util.Locale

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.wire.Wire
import com.google.common.base.Charsets
import com.typesafe.scalalogging.LazyLogging
import org.json4s.{JValue, Reader}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Uz API station.
  *
  * @param wire wire to API
  */
class StationsUz(private val wire: Wire[Req, JValue])(implicit mt: Materializer)
  extends Stations with Json with LazyLogging {

  implicit object Reader extends Reader[Station] {
    override def read(value: JValue): Station = Station(
      id = (value \ "station_id").as[String],
      name = (value \ "title").as[String]
    )
  }

  override def stationsByName(name: String, local: Locale = Locale.ENGLISH): Future[Iterable[Station]] = {
    logger.debug("call #stationsByName({})", name)
    val encName = URLEncoder.encode(name, Charsets.UTF_8.name())
    val req: HttpRequest = RequestBuilding.Get(s"/ru/purchase/station/$encName/")
    val stations: Future[Seq[Station]] = Source.single(req -> name)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => json.as[List[Station]])
    stations
  }

}
