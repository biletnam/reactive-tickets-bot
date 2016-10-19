package org.tickets.railway

import org.tickets.model.TicketsCriteria

import scala.concurrent.Future

trait RailwayTickets {

  /**
    * Find tickets by given criteria.
    * @return found tickets.
    */
  def findTickets(criteria: TicketsCriteria): Future[List[Any]]

}
