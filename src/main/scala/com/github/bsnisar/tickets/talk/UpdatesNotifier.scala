package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.talk.UpdatesNotifier.AcceptNotify
import com.github.bsnisar.tickets.telegram.{Msg, TelegramUpdates}
import com.github.bsnisar.tickets.telegram.TelegramUpdates.{Reply, Updates}
import com.typesafe.scalalogging.LazyLogging

object UpdatesNotifier {

  def props(telegramRef: ActorRef): Props = Props(classOf[UpdatesNotifier], telegramRef)

  /**
    * Accept of an update.
    * @param id accepted id
    * @param msg maybe message for telegram.
    */
  case class AcceptNotify(id: Int, msg: Option[Reply] = None)
}

class UpdatesNotifier(val telegramRef: ActorRef, val hub: ActorRef) extends Actor with LazyLogging {
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

    case AcceptNotify(seq, msg) =>
      lastSeqNum = math.max(lastSeqNum, seq)
      if (msg.isDefined) {
        telegramRef ! msg.get
      }
  }

  private def dispatch(msg: TelegramUpdates.Update): Unit = hub ! msg
}
