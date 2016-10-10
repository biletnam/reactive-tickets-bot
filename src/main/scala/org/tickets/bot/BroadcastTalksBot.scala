package org.tickets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.json4s.JValue
import org.tickets.bot.BroadcastTalksBot.TalkId
import org.tickets.misc.LogSlf4j

object BroadcastTalksBot {

  def props =
    Props(classOf[BroadcastTalksBot])

  private case class TalkId(chat: String, user: String)
}

/**
  * Broadcast that push messages per client talks.
  */
class BroadcastTalksBot extends Actor with LogSlf4j {
  override def receive: Receive = onTelegramUpdate()

  /**
    * Push notifications to Telegram.
    */
  private val push = context.actorSelection("/telegram/push")

  /**
    * Talks.
    */
  private var talks: Map[TalkId, ActorRef] = Map.empty


  private def onTelegramUpdate(): Receive = {
    case update: JValue =>
      val msg = update \ "message"
      val updateId = update \ "update_id"
      onTelegramMessage(msg)
  }

  private def onTelegramMessage(msg: JValue): Unit = {
    import org.json4s.DefaultReaders._
    val chatId = (msg \ "chat" \ "id").as[String]
    val userId = (msg \ "from" \ "id").as[String]
    val ref = getTalk(TalkId(chatId, userId))
    ref ! msg
  }

  private def getTalk(roomId: TalkId) : ActorRef = talks.get(roomId) match {
    case Some(ref) => ref
    case None =>
      val ref = context.actorOf(TalkBot.props(roomId.chat), s"/c${roomId.chat}/u${roomId.user}")
      talks = talks + (roomId -> ref)
      ref
  }
}
