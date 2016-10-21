package org.tickets.railway

import org.tickets.model.{Train, TrainCriteria}

import scala.concurrent.Future

/**
  * Railway trains.
  */
trait RailwayTrains {

  /**
    * Find all train tickets for given criteria.
    * @param criteria criteria for search
    * @return result of search.
    */
  def findTrains(criteria: TrainCriteria): Future[Train]
}
