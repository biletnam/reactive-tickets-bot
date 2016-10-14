package org.tickets.railway
import akka.actor.ActorRef
import org.tickets.Station

import scala.concurrent.{Future, Promise}

/**
  * Created by bsnisar on 14.10.16.
  */
class UzRailwayStations(ref: ActorRef) extends RailwayStations {

  override def findStations(byName: String): Future[List[Station]] = {
    val promise = Promise[List[Station]]()
    ref ! UzFindStationsReq.request(byName, promise)
    promise.future
  }
}
