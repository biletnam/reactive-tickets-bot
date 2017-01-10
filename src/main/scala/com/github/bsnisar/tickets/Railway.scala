package com.github.bsnisar.tickets

/**
  * Railway system.
  *
  * @author bsnisar
  */
trait Railway {

  def trains: Trains

  /**
    * Stations.
    * @return stations
    */
  def stations: Stations
}
