package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, Props}
import com.github.bsnisar.tickets.talk.Answers.Answer
import com.github.bsnisar.tickets.telegram.{Msg, TelegramPush}
import com.typesafe.scalalogging.LazyLogging


object Answers {

  def props(tgPush: TelegramPush): Props = Props(classOf[Answers], tgPush)

  trait Answer {
    def chatID: String
    def msg: Msg
  }

  /**
    * Reply to message.
    * @param chatID chat to write
    * @param msg message
    */
  case class AnswerBean(chatID: String, msg: Msg) extends Answer
}

class Answers(val tgPush: TelegramPush) extends Actor with LazyLogging {
  override def receive: Receive = {
    case reply: Answer =>
      logger.debug(s"push $reply message")
      tgPush.push(reply)
  }
}
