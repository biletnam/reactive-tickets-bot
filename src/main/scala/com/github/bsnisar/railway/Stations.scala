package com.github.bsnisar.railway

import scala.concurrent.Future

trait Stations {

  /**
    * Find stations by its name.
    *
    * @param name name like
    * @return list of stations
    */
  def stationsByName(name: String): Future[Seq[Station]]
}
