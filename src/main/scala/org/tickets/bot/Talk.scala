package org.tickets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Telegram.ChatId
import org.tickets.telegram.TelegramPush
import org.tickets.telegram.TelegramPush.Msg

object Talk {
  def props(telegram: ActorRef, id: ChatId): Props
    = Props(classOf[Talk], telegram, id)
}


class Talk(telegram: ActorRef, id: ChatId) extends Actor with LogSlf4j {
  override def receive: Receive = commonControls()


  private def commonControls(): Receive = {
    case "/help" =>
      telegram ! createMessage("Hello world! This is bot for searching tickets")
    case "/route" =>
      telegram ! createMessage(
        """
          |"Please specify parameters for searching:"
          |  /from <name> - departure station name (part of name)
          |  /to <name> - departure station name (part of name)
        """.stripMargin)
  }

  private def createMessage(text: String): Msg = {
    TelegramPush.TextMsg(id, text)
  }
}