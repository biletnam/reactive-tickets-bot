package org.tickets.railway

import org.tickets.Station
import org.tickets.Station.StationId

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
  def findStations(byName: String): Future[List[Station]]

  /**
    * Station by given id.
    * @param id station id
    * @return station
    */
  def station(id: StationId): Future[Station]
}
