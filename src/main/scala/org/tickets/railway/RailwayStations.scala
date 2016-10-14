package org.tickets.railway

import org.tickets.Station

import scala.concurrent.Future

trait RailwayStations {

  def findStations(byName: String): Future[List[Station]]

}
