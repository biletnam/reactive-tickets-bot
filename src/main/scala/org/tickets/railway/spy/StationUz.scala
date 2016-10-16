package org.tickets.railway.spy
import org.json4s.Reader
import org.tickets.railway.spy.Station.StationId

/**
  * Station from UZ API.
  * @author bsnisar
  */
case class StationUz(apiId: String, name: String) extends Station {
  override def identifier: StationId = apiId
}

object StationUz {
  implicit object StationReader extends Reader[Station] {
    import org.json4s._
    import org.tickets.misc.JsonSupport._

    def read(json: JValue): Station = StationUz(
      apiId = (json \ "station_id").extract[String],
      name = (json \ "title").extract[String]
    )
  }
}