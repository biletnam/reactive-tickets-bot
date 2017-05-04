package com.github.bsnisar.tickets.talk

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.github.bsnisar.tickets.telegram.TelegramPull
import com.github.bsnisar.tickets.telegram.TelegramPull.Event
import com.typesafe.scalalogging.LazyLogging

object Updates {

  def props(tg: TelegramPull, hub: ActorRef)(implicit mt: Materializer): Props =
    Props(classOf[Updates], tg, hub, mt)

  case object Tick
}

class Updates(telegramPull: TelegramPull,
              hub: ActorRef)(implicit m: Materializer) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher
  private var lastSeqNum = 0

  override def receive: Receive = {
    case Updates.Tick =>
      telegramPull.pull(lastSeqNum)
        .pipeTo(self)

    case Failure(err) =>
      logger.error("pull failed", err)

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
