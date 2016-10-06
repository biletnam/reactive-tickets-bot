package org.tickets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.json4s.JsonAST.JValue
import org.tickets.bot.ChatBot.RoomId
import org.tickets.misc.ActorSlf4j

object ChatBot {
  def props = Props(classOf[ChatBot])

  private case class RoomId(chat: String, user: String)
}

class ChatBot(conversationProps: Props) extends Actor with ActorSlf4j {
  private val push = context.actorSelection("/telegram/push")

  private var rooms: Map[RoomId, ActorRef] = Map.empty

  override def receive: Receive = {
    case "/route" =>

    case msg: JValue =>
      import org.json4s.DefaultReaders._
      val chatId = (msg \ "chat" \ "id").as[String]
      val userId = (msg \ "from" \ "id").as[String]
      val ref = getTalk(RoomId(chatId, userId))
      ref ! msg
  }


  private def getTalk(roomId: RoomId) : ActorRef = {
    if (!rooms.contains(roomId)) {
      val ref = context.actorOf(conversationProps, s"/c${roomId.chat}/u${roomId.user}")
      rooms = rooms + (roomId -> ref)
      ref
    } else {
      rooms(roomId)
    }
  }
}
