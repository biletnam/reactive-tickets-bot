package org.tickets.bot

import akka.actor.{Deploy, Props}
import akka.stream.actor.ActorPublisher
import org.tickets.misc.HttpSupport.Request

object TelegramPublisher {
  def props: Props = Props(classOf[TelegramPublisher])
    .withDeploy(Deploy(path = "/telegram/push"))
}

class TelegramPublisher extends ActorPublisher[Request] {
  override def receive: Receive = {
    case "" => ???
  }
}
