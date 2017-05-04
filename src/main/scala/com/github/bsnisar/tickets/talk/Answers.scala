package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, Props}
import com.github.bsnisar.tickets.talk.Answers.Reply
import com.github.bsnisar.tickets.telegram.{Msg, TelegramPush}


object Answers {

  def props(tgPush: TelegramPush): Props = Props(classOf[Answers], tgPush)

  /**
    * Reply to message.
    * @param chatID chat to write
    * @param msg message
    */
  case class Reply(chatID: String, msg: Msg)
}

class Answers(val tgPush: TelegramPush) extends Actor {
  override def receive: Receive = {
    case reply: Reply => tgPush.push(reply)
  }
}
