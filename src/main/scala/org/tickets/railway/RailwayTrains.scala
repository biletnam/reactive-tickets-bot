package org.tickets.railway

import org.tickets.model.{Train, TrainCriteria}

import scala.concurrent.Future

trait RailwayTrains {


  def findTrains(criteria: TrainCriteria): Future[Train]
}
