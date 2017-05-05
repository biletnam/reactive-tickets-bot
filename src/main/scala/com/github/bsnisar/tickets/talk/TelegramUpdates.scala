package com.github.bsnisar.tickets.talk

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.github.bsnisar.tickets.telegram.TelegramPull
import com.github.bsnisar.tickets.telegram.TelegramPull.UpdatesEvent
import com.typesafe.scalalogging.LazyLogging

object TelegramUpdates {

  def props(tg: TelegramPull, hub: ActorRef)(implicit mt: Materializer): Props =
    Props(classOf[TelegramUpdates], tg, hub, mt)

  case object Tick
}

class TelegramUpdates(telegramPull: TelegramPull,
                      hub: ActorRef)(implicit m: Materializer) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher

  private var lastSeqNum = 0

  override def receive: Receive = {
    case TelegramUpdates.Tick =>
      telegramPull.pull(lastSeqNum)
        .pipeTo(self)

    case Failure(err) =>
      logger.error(s"#pull failed", err)

    case UpdatesEvent(messages) =>
        messages foreach {
          case msg if msg.seqNum >= lastSeqNum =>
            logger.debug(s"dispatch: offset = $lastSeqNum, msg = $msg")
            lastSeqNum = math.max(lastSeqNum, msg.seqNum + 1)
            hub ! msg
          case msg =>
            logger.debug(s"skip: offset message sequence ${msg.seqNum} < $lastSeqNum")
        }
  }
}
