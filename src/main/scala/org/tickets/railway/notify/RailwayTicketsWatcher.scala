package org.tickets.railway.notify



import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream._
import akka.stream.actor.{ActorSubscriber, RequestStrategy}
import akka.stream.scaladsl._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.tickets.db.SubscriptionSchema.Observer
import org.tickets.model.{Train, TrainCriteria}
import org.tickets.railway.RailwayApi
import org.tickets.railway.RailwayApi.{Req, Res}
import org.tickets.railway.uz.ApiRequestsUZ

import scala.concurrent.{ExecutionContext, Future}

object RailwayTicketsWatcher {

  /**
    * Request for observing new tickets.
    * @param observer request owner
    * @param criteria criteria for search
    */
  case class InspectNext(observer: Observer, criteria: TrainCriteria)


  /**
    * Result of searching
    * @param trains found trains
    */
  case class ApiResponse(trains: List[Train] = List.empty)

}

class RailwayTicketsWatcher(implicit ex: ExecutionContext, as: ActorSystem, mt: Materializer) extends Json4sSupport{

  val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val in: SourceShape[Req] = builder.add(requests)
    val mkHttpResp: FlowShape[Req, Res] = builder.add(apiFlow)
    val asTrain = RailwayApi.httpRespFlowAsync(toTrain)

    in ~> mkHttpResp ~> asTrain

    ClosedShape
  })

  lazy val src: Source[TrainCriteria, ActorRef] =
    Source.actorRef[TrainCriteria](100, OverflowStrategy.dropHead)

  lazy val requests = src
    .map(mkSearchRequests)
    .mapConcat(identity)

  lazy val apiFlow: RailwayApi.ApiFlow = RailwayApi.httpFlowUzApi

  private def mkSearchRequests(criteria: TrainCriteria): List[Req] = criteria.arrivals.map(time =>
    ApiRequestsUZ.createFindTickets(
      fromId = criteria.fromStation.apiCode,
      toId = criteria.toStation.apiCode,
      arrive = time)
  )

  private def toTrain(rsp: HttpResponse): Future[Train] = {
    import Train._
    import org.tickets.misc.JsonSupport._

    Unmarshal(rsp.entity).to[Train]
  }
}

