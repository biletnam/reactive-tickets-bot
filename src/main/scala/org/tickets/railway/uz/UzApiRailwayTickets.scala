package org.tickets.railway.uz

import org.tickets.railway.RailwayTickets
import org.tickets.model.TicketsCriteria
import org.tickets.railway.RailwayApi.ApiFlow

import scala.concurrent.Future

class UzApiRailwayTickets(val httpFlow: ApiFlow) extends RailwayTickets {

  override def findTickets(criteria: TicketsCriteria): Future[List[Any]] = {
    ???
  }
}
