package org.tickets

import com.google.common.base.Strings
import com.google.common.primitives.Longs
import org.json4s.Reader
import java.lang.{Long => jLong}
import java.util.concurrent.ThreadLocalRandom

import org.hashids.Hashids
import org.tickets.Station.StationId

/**
  * Station API model.
  *
  * @param id station id
  * @param name station name
  * @author bsnisar
  */
case class Station(id: String, name: String) {

  def identifier: StationId = {
    val maybeId: jLong = Longs.tryParse(id)
    jLong.toHexString(maybeId)
  }

}

/**
  * Companion object for Station
 *
  * @author bsnisar
  */
object Station {
  type StationId = String

  val Hash = new Hashids("stations,lol")

  implicit object StationReader extends Reader[Station] {
    import org.json4s._
    import org.tickets.misc.JsonSupport._

    def read(json: JValue): Station = Station(
      id = (json \ "station_id").extract[String],
      name = (json \ "title").extract[String]
    )
  }

}