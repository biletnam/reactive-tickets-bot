package com.github.bsnisar.tickets.telegram.actor

import akka.actor.{Actor, ActorRef, Props, Status}
import akka.stream.Materializer
import com.github.bsnisar.tickets.telegram.Telegram
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Updates
import com.github.bsnisar.tickets.telegram.actor.TelegramPull.Tick
import com.typesafe.scalalogging.LazyLogging

object TelegramPull {

  def props(tgUpdates: Telegram, materializer: Materializer,  hub: ActorRef): Props =
    Props(classOf[TelegramPull], tgUpdates, hub, materializer)

  case object Tick
}
class TelegramPull(val tgUpdates: Telegram,
                   val hub: ActorRef)(implicit val m: Materializer) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher

  private[this] var offset = 1

  override def receive: Receive = {
    case Tick =>
      tgUpdates.pull(offset).pipeTo(self)
    case messages @ Updates(seq, _) =>
      offset = math.max(seq, offset)
      hub ! messages
    case Status.Failure(error) =>
      logger.error("pull call failed", error)
  }
}
