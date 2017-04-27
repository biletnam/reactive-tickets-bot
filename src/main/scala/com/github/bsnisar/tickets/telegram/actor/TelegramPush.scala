package com.github.bsnisar.tickets.telegram.actor

import akka.actor.{Actor, Props}
import akka.stream.Materializer
import com.github.bsnisar.tickets.misc.{Json, Templates}
import com.github.bsnisar.tickets.telegram.{Telegram, TelegramUpdates}

object TelegramPush {
  def props(telegram: Telegram, templates: Templates)
           (implicit m: Materializer): Props = Props(classOf[TelegramPush], telegram, templates, m)
}

class TelegramPush(val telegram: Telegram, val template: Templates)(implicit m: Materializer) extends Actor with Json {
  import context.dispatcher

  override def receive: Receive = {
    case reply @ TelegramUpdates.Reply =>
      telegram.push(reply)
  }
}
