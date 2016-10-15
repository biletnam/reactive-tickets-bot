package org.tickets.railway
import akka.http.scaladsl.util.FastFuture
import com.google.common.cache.{Cache, CacheBuilder}
import org.tickets.Station
import org.tickets.Station.StationId

import scala.concurrent.Future

class CacheRailwayStations(val origin: RailwayStations) extends RailwayStations {

  private lazy val cash: Cache[StationId, Station] =
    CacheBuilder
      .newBuilder()
      .maximumSize(400)
      .build[StationId, Station]()

  override def findStations(byName: String): Future[List[Station]] = {
    import FastFuture._
    val stations: Future[List[Station]] = origin.findStations(byName)

    stations.fast.foreach(_.foreach(station =>
      cash.put(station.identifier, station))
    )

    stations
  }

  override def station(id: StationId): Future[Station] = {
    val station: Station = cash.getIfPresent(id)
    if (station == null) {
      origin.station(id)
    } else {
      Future.successful(station)
    }
  }
}
