package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.github.bsnisar.tickets.talk.TgResponses.Reply
import com.github.bsnisar.tickets.telegram.TelegramPull
import com.github.bsnisar.tickets.telegram.TelegramPull.Event
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Update
import com.typesafe.scalalogging.LazyLogging

object TgUpdates {

  def props(tg: TelegramPull, telegramRef: ActorRef, hub: ActorRef): Props =
    Props(classOf[TgUpdates], tg, telegramRef, hub)

  case object Tick
}

class TgUpdates(tg: TelegramPull,
                hub: ActorRef)(implicit m: Materializer) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher
  private var lastSeqNum = 0

  override def receive: Receive = {
    case "tick" =>
      tg.pull(lastSeqNum)
        .pipeTo(self)

    case Event(messages) =>
        messages foreach {
          case msg if msg.seqNum > lastSeqNum =>
            logger.trace(s"dispatch: offset = $lastSeqNum, msg = $msg")
            hub ! msg
            lastSeqNum = math.max(lastSeqNum, msg.seqNum)
          case msg =>
            logger.trace(s"skip, offset [$lastSeqNum] > message sequence ${msg.seqNum}")
        }
  }
}
