package com.github.bsnisar.tickets.talk


import akka.actor.{Actor, ActorContext, ActorRef, Props}
import com.github.bsnisar.tickets.talk.Talks.BotFactory
import com.github.bsnisar.tickets.telegram.TelegramMessages.{Update, Updates}
import com.typesafe.scalalogging.LazyLogging

object Talks {
  def props(factory: BotFactory, stationsSearcher: ActorRef): Props =
    Props(classOf[Talks], factory, stationsSearcher)

  trait BotFactory {
    def create(name: String, chatID: String)(implicit ac: ActorContext): ActorRef
  }



}

/**
  * Chats router. Delegate each message separate handler.
  * If there is no available bot for given chat, create new one.
  */
final class Talks(factory: BotFactory, val stationsSearcher: ActorRef) extends Actor with LazyLogging {
  private var chats = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case update: Update =>
      val chatID = update.chat
      chats.get(chatID) match {
        case Some(ref) =>
          ref ! update
        case None =>
          val botName = s"chat::$chatID"
          logger.debug("new bot {} created", botName)
          val ref = factory.create(botName, chatID)
          chats = chats + (chatID -> ref)
          ref ! update
      }
  }
}
