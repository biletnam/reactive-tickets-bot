package org.tickets.railway.model

import org.json4s.Reader

case class Station(uid: String, apiCode: String, name: String, provider: String)

object Station {
  implicit object UzStationReader extends Reader[Station] {
    import org.json4s._
    import org.tickets.misc.JsonSupport._

    def read(json: JValue): Station = {
      val stationId = (json \ "station_id").extract[String]
      val name: String = (json \ "title").extract[String]
      val uid = stationId.toLong.toHexString

      Station(
        uid = uid,
        apiCode = stationId,
        name = name,
        provider = "uz"
      )

    }
  }

  def mock(id: String, name: String): Station =
    Station(id, id, name, "uz")
}
