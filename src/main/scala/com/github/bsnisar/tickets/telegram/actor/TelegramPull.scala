package com.github.bsnisar.tickets.telegram.actor

import akka.actor.{Actor, ActorRef, Props, Status}
import com.github.bsnisar.tickets.telegram.TelegramUpdates
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Updates
import com.github.bsnisar.tickets.telegram.actor.TelegramPull.Tick
import com.typesafe.scalalogging.LazyLogging

object TelegramPull {

  def props(tgUpdates: TelegramUpdates, hub: ActorRef): Props =
    Props(classOf[TelegramPull], tgUpdates, hub)

  case object Tick
}
class TelegramPull(val tgUpdates: TelegramUpdates, val hub: ActorRef) extends Actor with LazyLogging {
  import context.dispatcher
  import akka.pattern.pipe

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
