package org.tickets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Telegram.ChatId
import org.tickets.telegram.{Push, Update}

object Talk {
  def props(telegram: ActorRef, id: ChatId): Props
    = Props(classOf[Talk], telegram, id)
}

class Talk(telegram: ActorRef, id: ChatId) extends Actor with LogSlf4j {
  override def receive: Receive = {
    case update: Update =>
      controlCommands orElse specificCommands
  }

  /**
    * Common control commands.
    */
  def controlCommands: Receive = {
    case update: Update => update.text match {
      case "/start" =>
        log.trace("[{}] on start command", id)
        telegram ! Push.TextMsg(id, "Hello world!")
    }
  }

  /**
    * Specific control commands
    * @return
    */
  final def specificCommands: Receive = {
    case _ => Unit
  }



}
