package com.github.bsnisar.tickets.telegram.actor

import akka.actor.{Actor, Props}
import akka.stream.Materializer
import com.github.bsnisar.tickets.misc.{Json, Templates}
import com.github.bsnisar.tickets.telegram.{TelegramPush, TelegramUpdates}

object PushActor {
  def props(telegram: TelegramPush, templates: Templates)
           (implicit m: Materializer): Props = Props(classOf[PushActor], telegram, templates, m)
}

class PushActor(val telegram: TelegramPush, val template: Templates)(implicit m: Materializer) extends Actor with Json {
  import context.dispatcher

  override def receive: Receive = {
    case reply: TelegramUpdates.Reply =>
      telegram.push(reply)
  }
}