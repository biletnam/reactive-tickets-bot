package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef}
import com.github.bsnisar.tickets.telegram.TelegramUpdates
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Updates
import com.typesafe.scalalogging.LazyLogging

object UpdatesNotifier {

  case class AcceptNotify(id: Int)
}

class UpdatesNotifier extends Actor with LazyLogging {

  private var lastSeqNum = Int.MinValue

  override def receive: Receive = {
    case Updates(seq, messages) =>
      if (seq > lastSeqNum) {
        messages foreach {
          case msg if msg.seqNum > lastSeqNum =>
            logger.trace(s"dispatch, offset = $lastSeqNum, msg.seqNum = ${msg.seqNum}")
            dispatch(msg)
          case msg =>
            logger.trace(s"skip, offset [$lastSeqNum] > message sequence ${msg.seqNum}")
        }

        lastSeqNum = math.max(lastSeqNum, seq)
      } else {
        logger.debug(s"skip messages, with already consumed offset $seq")
      }
  }

  private def dispatch(msg: TelegramUpdates.Update): Unit = ???
}
