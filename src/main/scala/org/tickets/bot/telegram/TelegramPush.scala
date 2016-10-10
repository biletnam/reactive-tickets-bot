package org.tickets.bot.telegram

import akka.actor.{Deploy, Props}
import akka.stream.actor.ActorPublisher
import org.tickets.misc.HttpSupport.Request

object TelegramPush {

  def props: Props = Props(classOf[TelegramPush])
    .withDeploy(Deploy(path = "/telegram/push"))

  /**
    * Push message to bot.
    * @param test
    */
  case class PushText(test: String)



}

class TelegramPush extends ActorPublisher[Request] {
  override def receive: Receive = {
    case "" => ???
  }
}
