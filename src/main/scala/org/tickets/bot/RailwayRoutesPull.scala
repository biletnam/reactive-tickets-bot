package org.tickets.bot

import akka.actor.{Actor, Props}
import org.tickets.bot.RailwayRoutesPull.PullNext
import org.tickets.misc.LogSlf4j

object RailwayRoutesPull {

  def props: Props = Props[RailwayRoutesPull]

  /**
    * Pull railway api for available routes
    */
  case object PullNext
}

class RailwayRoutesPull extends Actor with LogSlf4j {
  override def receive: Receive = pulling()

  def pulling(): Receive = {
    case PullNext =>
      log.debug("#pulling pull available tickets")
  }
}
