package com.github.bsnisar.tickets.talk

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorRef, Props}
import akka.stream.Materializer
import com.github.bsnisar.tickets.talk.UpdatesProducer.{Continue, PollTick}
import com.github.bsnisar.tickets.telegram.UpdatesSource
import com.github.bsnisar.tickets.telegram.UpdatesSource.UpdatesEvent
import com.typesafe.scalalogging.LazyLogging

object UpdatesProducer {

  def props(tg: UpdatesSource, hub: ActorRef)(implicit mt: Materializer): Props =
    Props(classOf[UpdatesProducer], tg, hub, mt)

  case object Tick


  /**
    * Message signifying acknowledgement that upstream can send the next
    * item.
    */
  case object Continue

  /**
    * Message used by the producer for continuously polling the
    * data-source, while in the polling state.
    */
  case object PollTick
}

class UpdatesProducer(updatesSource: UpdatesSource,
                      hub: ActorRef)(implicit m: Materializer) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher

  private var lastSeqNum = 0

  override def receive: Receive = {
    case UpdatesProducer.Tick =>
      updatesSource.pull(lastSeqNum)
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
