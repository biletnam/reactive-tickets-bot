package org.tickets.railway

import org.tickets.db.SubscriptionSchema.Observer
import org.tickets.model.TrainCriteria

import scala.concurrent.Future

/**
  * Railway tickets,
  */
trait RailwaySubscription {

  /**
    * Save subscription for given criteria.
    * @param chatId chat id, that will be notified on results
    * @param criteria criteria for search
    * @return result of adding
    */
  def subscribe(chatId: Long, criteria: TrainCriteria): Future[Observer]
}

