package com.github.bsnisar.tickets
import java.time.{LocalDateTime, ZonedDateTime}

import scala.concurrent.Future
import scala.xml.{Node, NodeSeq}

/**
  * Trains from Uz API.
  * @author bsnisar
  */
class UzTrains extends Trains{
  override def find(query: NodeSeq): Future[Iterable[Train]] = {
    val routes = query \\ "routes" \ "route"
    routes.foreach(route)
    ???
  }

  private def route(route: Node): Unit = {
    val from = route \@ "from"
    val to = route \@ "to"
    val arrivals = route \ "arrives" \ "arrive" map(node => asTime(node.text))

    throw new IllegalArgumentException(s"no $arrivals")
  }

  private def asTime(s: String): LocalDateTime = {
    ZonedDateTime.parse(s).toLocalDateTime
  }

}
