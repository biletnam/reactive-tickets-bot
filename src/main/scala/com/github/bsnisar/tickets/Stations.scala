package com.github.bsnisar.tickets

import java.util.Locale

import scala.concurrent.Future

trait Stations {

  /**
    * Find stations by its name.
    *
    * @param name name like
    * @return list of stations
    */
  def stationsByName(name: String): Future[Iterable[Station]]
}
