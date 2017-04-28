package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, Props}
import com.github.bsnisar.tickets.telegram.{Msg, TelegramPush}


object TgResponses {

  def props(tgPush: TelegramPush): Props = Props(classOf[TgResponses], tgPush)

  /**
    * Reply to message.
    * @param chatID chat to write
    * @param msg message
    */
  case class Reply(chatID: String, msg: Msg)
}

class TgResponses(val tgPush: TelegramPush) extends Actor {
  override def receive: Receive = ???
}
