package org.tickets.bot

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Telegram.ChatId
import org.tickets.telegram.{TelegramPull, Update, Updates}

object Talks {
  def props(propsFactory: ChatId => Props): Props = Props(classOf[Talks], propsFactory)
}

/**
  * Route particular updates to a Room bots.
  * @param propsFactory ref for pushing messages
  */
class Talks(val propsFactory: ChatId => Props) extends Actor with LogSlf4j {

  private var chats: Map[Long, ActorRef] = Map.empty

  override def receive: Receive = {
    case updates: Updates if updates.empty =>
      log.debug("#updates: content is empty")
    case updates: Updates =>
      routeAndSend(updates)
      sender() ! TelegramPull.Ack(updates.lastId)
  }

  private def routeAndSend(updates: Updates): Unit = {
    for (update <- updates.messages) {
      chats.get(update.chat) match {
        case Some(ref) =>
          send(update, ref)
        case None =>
          val ref = context.actorOf(AtMostOneUpdate.props(
            propsFactory(update.chat)
          ), s"usr${update.user}@${update.chat}")
          chats = chats + (update.chat -> ref)
          log.debug("#updates: new root {}, total rooms {}", ref, chats.size)
          send(update, ref)
      }
    }
  }

  private def send(update: Update, ref: ActorRef): Unit = {
    ref ! update
  }

}
