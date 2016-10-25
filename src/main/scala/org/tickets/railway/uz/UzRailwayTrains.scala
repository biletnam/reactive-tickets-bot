package org.tickets.railway.uz

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.json4s.JValue
import org.json4s.JsonAST.JArray
import org.tickets.model.{Train, TrainCriteria}
import org.tickets.railway.RailwayApi._
import org.tickets.railway.RailwayTrains

import scala.concurrent.{ExecutionContext, Future}

/**
  * UZ API for calling search for available trains.
  * @param apiFlow http connection pool to host
  */
class UzRailwayTrains(apiFlow: ApiFlow)
                     (implicit ex: ExecutionContext, mt: Materializer) extends RailwayTrains {

  override def findTrains(criteria: TrainCriteria): Future[Seq[Train]] = {

    val apiRequests: Seq[Req] = criteria.arrivals.map(arriveTime => ApiRequestsUZ.createFindTickets(
      fromId = criteria.fromStation.apiCode,
      toId = criteria.toStation.apiCode,
      arrive = arriveTime))

    val trainsParser: Flow[JValue, List[Train], Any] = Flow[JValue].map { json =>
      ApiRequestsUZ.readContent(json) match {
        case JArray(values) => values.map(_.as[Train])
      }
    }

    val responses: Future[Seq[List[Train]]] =
      Source.fromIterator(() => apiRequests.iterator)
        .via(apiFlow)
        .via(mapHttpResponse(asJSON))
        .via(trainsParser)
        .runWith(Sink.seq)

    responses.map(_.flatten)
  }
}
