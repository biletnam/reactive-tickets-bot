package org.tickets.railway


import scala.concurrent.Future

/**
  * Railway stations.
  */
trait RailwayStations {

  /**
    * Find stations by given name.
    * @param byName like name
    * @return stations
    */
  def findStations(byName: String): Future[List[model.Station]]
}
