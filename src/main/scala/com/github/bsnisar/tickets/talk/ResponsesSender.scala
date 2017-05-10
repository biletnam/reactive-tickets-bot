package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, Props}
import com.github.bsnisar.tickets.talk.ResponsesSender.Reply
import com.github.bsnisar.tickets.telegram.{Msg, TelegramPush}
import com.typesafe.scalalogging.LazyLogging


object ResponsesSender {

  def props(tgPush: TelegramPush): Props = Props(classOf[ResponsesSender], tgPush)

  /**
    * Reply to message.
    * @param chatID chat to write
    * @param msg message
    */
  case class Reply(chatID: String, msg: Msg)
}

class ResponsesSender(val tgPush: TelegramPush) extends Actor with LazyLogging {
  override def receive: Receive = {
    case reply: Reply =>
      logger.debug(s"push $reply message")
      tgPush.push(reply)
  }
}
