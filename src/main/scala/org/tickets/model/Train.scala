package org.tickets.model

import java.time.LocalDateTime

import org.json4s.{JValue, Reader}

/**
  * Train and tickets for it.
  *
  * @param id
  * @param code
  * @param departure
  * @param arrive
  * @param tickets
  */
case class Train(id: String, code: String, departure: LocalDateTime, arrive: LocalDateTime, tickets: Seq[Ticket])

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