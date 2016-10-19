package org.tickets.railway.spy
import org.json4s.Reader

object StationUz {
  implicit object StationReader extends Reader[Station] {
    import org.json4s._
    import org.tickets.misc.JsonSupport._

    def read(json: JValue): Station = {
      val stationId = (json \ "station_id").extract[String]
      val name: String = (json \ "title").extract[String]
      val uid = stationId.toLong.toHexString

      StationConst(
        uid = uid,
        apiCode = stationId,
        name = name,
        provider = "uz"
      )

    }
  }
}