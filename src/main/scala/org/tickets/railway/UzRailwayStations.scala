package org.tickets.railway
import java.util.concurrent.TimeoutException

import akka.actor.{ActorRef, ActorSystem}
import org.tickets.Station

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration._

/**
  * Search throw UZ railway API
  */
class UzRailwayStations(ref: ActorRef, sys: ActorSystem)(implicit ec: ExecutionContext = sys.dispatcher) extends RailwayStations {

  override def findStations(byName: String): Future[List[Station]] = {
    val promise = Promise[List[Station]]()
    ref ! UzFindStationsReq.request(byName, promise)
    sys.scheduler.scheduleOnce(6.seconds, new TtlTask(promise))
    promise.future
  }
}

class TtlTask(val p: Promise[_]) extends Runnable {
  override def run(): Unit = {
      p.tryFailure(new TimeoutException("ttl for promise is reached"))
  }
}
