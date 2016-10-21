package org.tickets.model

import org.json4s.Reader
import org.json4s._
import org.tickets.misc.JsonSupport._

/**
  * Default station in system.
  *
  * @param uid system id
  * @param apiCode api id
  * @param name name
  * @param provider provider
  */
case class Station(uid: String, apiCode: String, name: String, provider: String)

object Station {

  /**
    * Json converter for UZ api response
    */
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

  def mock(id: String, name: String): Station = Station(id, id, name, "uz")

  /**
    * Default JSON convertes.
    */
  object DefaultConversions {
    implicit object DefaultStationReader extends Reader[Station] {
      def read(json: JValue): Station = {
        val uid = (json \ "uid").extract[String]
        val apiCode: String = (json \ "apiCode").extract[String]
        val name: String = (json \ "name").extract[String]
        val provider: String = (json \ "provider").extract[String]

        Station(
          uid = uid,
          apiCode = apiCode,
          name = name,
          provider = "uz")
      }
    }

    implicit object DefaultStationWriter extends Writer[Station] {
      import org.json4s.JsonDSL._

      override def write(obj: Station): JValue = {
        ("uid" -> obj.uid) ~
          ("apiCode" -> obj.apiCode) ~
          ("name" -> obj.name) ~
          ("provider" -> obj.provider)
      }
    }
  }
}
