package org.tickets.model

import org.json4s.{JValue, Reader}

case class Train(id: String)

object Train {

  implicit object TrainUzReader extends Reader[Train] {
    import org.json4s._
    import org.tickets.misc.JsonSupport._

    override def read(value: JValue): Train = {
      val from = (value \ "from")
      val to = (value \ "till")
      val places = (value \ "types").extract[JArray]

      

      ???
    }
  }

}