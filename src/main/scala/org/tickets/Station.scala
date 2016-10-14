package org.tickets

import com.google.common.base.Strings
import com.google.common.primitives.Longs
import org.json4s.Reader
import java.lang.{Long => jLong}
import java.util.concurrent.ThreadLocalRandom

/**
  * Station API model.
  *
  * @param id station id
  * @param name station name
  * @author bsnisar
  */
case class Station(id: String, name: String) {

  def identifier(prefix: String = "st_"): String = {
    val maybeId: jLong = Longs.tryParse(id)
    if (maybeId != null) {
      s"$prefix${jLong.toHexString(maybeId)}"
    } else {
      s"$prefix${jLong.toHexString(ThreadLocalRandom.current().nextLong(10000))}"
    }
  }

}

/**
  * Companion object for Station
 *
  * @author bsnisar
  */
object Station {
  implicit object StationReader extends Reader[Station] {
    import org.json4s._
    import org.tickets.misc.HttpSupport.Json4sImplicits._

    def read(json: JValue): Station = Station(
      id = (json \ "station_id").extract[String],
      name = (json \ "title").extract[String]
    )
  }

}