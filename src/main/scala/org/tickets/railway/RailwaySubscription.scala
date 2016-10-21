package org.tickets.railway

import akka.actor.ActorRef
import org.tickets.model.{TrainCriteria$, Train}
import org.tickets.railway.RailwaySubscription.Request

import scala.concurrent.Future

/**
  * Railway tickets,
  */
trait RailwaySubscription {

  /**
    * Find trains by given criteria request.
    * @param request criteria request
    * @return found stations
    */
  def subscribe(request: Request): Future[List[Train]]
}


object RailwaySubscription {

  trait Request

  case class Watch(chatId: Long, criteria: TrainCriteria) extends Request

  case class FindOnce(ref: ActorRef, criteria: TrainCriteria) extends Request
}


