package com.github.bsnisar.tickets.actors

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.Update
import com.typesafe.scalalogging.LazyLogging

/**
  * Chats router. Delegate each message separate handler.
  * If there is no available bot for given chat, create new one.
  * @param prop bot props.
  */
final class Chats(private val prop: Props) extends Actor with LazyLogging {
  private var chats = Map.empty[Long, ActorRef]

  override def receive: Receive = {
    case update: Update =>
      val chatID = update.chat
      chats.get(chatID) match {
        case Some(ref) =>
          ref ! update

        case None =>
          val botName = s"ch_$chatID"
          logger.debug("create new bot {}", botName)
          val ref = context.actorOf(prop, botName)
          chats = chats + (chatID -> ref)
          ref ! update
      }
  }
}
