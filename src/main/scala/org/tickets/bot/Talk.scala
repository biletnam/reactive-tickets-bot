package org.tickets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Telegram.ChatId
import org.tickets.telegram.Update

object Talk {
  def props(telegram: ActorRef, id: ChatId): Props
    = Props(classOf[Talk], telegram, id)
}


class Talk(val telegram: ActorRef, val chatId: ChatId) extends Actor with LogSlf4j {
  private var lastUpdateSeqNum: Int = Int.MinValue
  private val talk: ActorRef = context.actorOf(RouteTalk.props(null, NotifierRef(chatId, telegram)))

  override def receive: Receive = {
    case update: Update if update.id > lastUpdateSeqNum =>
      lastUpdateSeqNum = update.id
      talk ! RouteTalk.Cmd(update.text, update)
  }
}