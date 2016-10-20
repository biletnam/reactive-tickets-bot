package org.tickets.railway

import akka.actor.ActorRef
import org.tickets.model.TicketsCriteria
import org.tickets.railway.RailwayTickets.Request

import scala.concurrent.Future


trait RailwayTickets {

  def subscribe(request: Request): Future[List[RailwayTickets]]
}

object RailwayTickets {
  trait Request

  case class Watch(chatId: Long, criteria: TicketsCriteria) extends Request

  case class FindOnce(ref: ActorRef, criteria: TicketsCriteria) extends Request
}


