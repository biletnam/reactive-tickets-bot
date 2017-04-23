package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.telegram.TgUpdate
import com.typesafe.scalalogging.LazyLogging

/**
  * Chats router. Delegate each message separate handler.
  * If there is no available bot for given chat, create new one.
  * @param prop bot props.
  */
final class Talks(private val prop: Props) extends Actor with LazyLogging {
  private var chats = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case update: TgUpdate =>
      val chatID = update.chat
      chats.get(chatID) match {
        case Some(ref) =>
          ref ! update
        case None =>
          val botName = s"chat::$chatID"
          logger.debug("new bot {} created", botName)
          val ref = context.actorOf(prop, botName)
          chats = chats + (chatID -> ref)
          ref ! update
      }
  }
}
