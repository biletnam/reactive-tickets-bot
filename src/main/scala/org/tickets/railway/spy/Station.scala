package org.tickets.railway.spy

import org.tickets.railway.spy.Station.StationId


object Station {
  type StationId = String
}

trait Station {

  /**
    * System unique id.
    * @return id
    */
  def identifier: StationId

  /**
    * API station id.
    * @return id specific for API
    */
  def apiId: String

  /**
    * Name of station. Can be in a different locale.
    * @return name
    */
  def name: String
}