package org.tickets.railway.notify

import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph}
import akka.stream.{ClosedShape, Materializer}
import org.tickets.db.SubscriptionSchema.Observer
import org.tickets.model.{Train, TrainCriteria}
import org.tickets.railway.RailwayApi.{Req, Res}
import org.tickets.railway.notify.RailwayTicketsWatcher.InspectNext

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

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

class RailwayTicketsWatcher(implicit ex: ExecutionContext, mt: Materializer) {


  def onHttpResponse = {
    Flow[InspectNext]
      .map(toUzHttpReq)
  }

  private def toUzHttpReq(next: InspectNext): Req = ???


  def stream(criteria: TrainCriteria): Unit = {
    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>



      ClosedShape
    })
  }


}
