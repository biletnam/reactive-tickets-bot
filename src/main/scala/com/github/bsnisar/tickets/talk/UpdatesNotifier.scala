package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.github.bsnisar.tickets.talk.UpdatesNotifier.AcceptNotify
import com.github.bsnisar.tickets.telegram.{Msg, Telegram, TelegramPull, TelegramUpdates}
import com.github.bsnisar.tickets.telegram.TelegramUpdates.{Reply, Updates}
import com.github.bsnisar.tickets.telegram.actor.PullActor
import com.typesafe.scalalogging.LazyLogging

object UpdatesNotifier {

  def props(tg: TelegramPull, telegramRef: ActorRef, hub: ActorRef): Props =
    Props(classOf[UpdatesNotifier], tg, telegramRef, hub)

  /**
    * Accept of an update.
    * @param id accepted id
    * @param msg maybe message for telegram.
    */
  case class AcceptNotify(id: Int, msg: Option[Reply] = None)
}

class UpdatesNotifier(tg: TelegramPull,
                      telegramPush: ActorRef,
                      hub: ActorRef)(implicit m: Materializer) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher
  private var lastSeqNum = 0

  override def receive: Receive = {
    case "tick" =>
      tg.pull(lastSeqNum).pipeTo(self)


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
        telegramPush ! msg.get
      }
  }

  private def dispatch(msg: TelegramUpdates.Update): Unit = hub ! msg
}
