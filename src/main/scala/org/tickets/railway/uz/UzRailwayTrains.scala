package org.tickets.railway.uz

import akka.NotUsed
import akka.stream.{ClosedShape, Materializer}
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Source, Zip}
import org.tickets.model.{Train, TrainCriteria}
import org.tickets.railway.RailwayApi.{ApiFlow, Res}
import org.tickets.railway.RailwayTrains
import org.tickets.telegram.TelegramApi.ChatId

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UzRailwayTrains(val uzApiFlow: ApiFlow)
                     (implicit ex: ExecutionContext, mt: Materializer) extends RailwayTrains {
  override def findTrains(criteria: TrainCriteria): Future[Train] = ???

  case class UzTrainsHit(subID: Int, chatID: Long)

  case class SearchContext(subID: Int, chatId: Long)

  val MaximumDistinctDestinations = 20
  type Task = String

  def stream(criteria: TrainCriteria): Unit = {
    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val in = builder.add(Source.single(criteria))

      ClosedShape
    })

  def toHitResponses(req: Res): Future[Any] = req match {
    case (Success(httpResponse), ctx: SearchContext) => ???
    case (Failure(err), ctx: SearchContext) => ???
    case _ => ???
  }


}


object UzRailwayTrains {

  case class SearchReq(criteria: TrainCriteria, chatId: ChatId)
}