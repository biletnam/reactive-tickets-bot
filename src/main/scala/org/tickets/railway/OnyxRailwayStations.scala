package org.tickets.railway
import org.tickets.Station
import org.tickets.Station.StationId

import scala.concurrent.Future

class OnyxRailwayStations(val origin: RailwayStations) extends RailwayStations {
  override def findStations(byName: String): Future[List[Station]] = ???

  override def station(id: StationId): Future[Station] = ???
}
