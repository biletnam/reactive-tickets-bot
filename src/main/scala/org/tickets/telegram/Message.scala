package org.tickets.telegram

import org.json4s.JValue
import org.json4s._
import org.tickets.misc.JsonSupport._

/**
  * Telegram Message API.
  *
  * @param id id of message
  * @param chat chat id
  * @param user username
  * @param text input text
  */
case class Message(id: Int, chat: Long, user: String, text: String)

object Message {
  implicit object TgUpdateReader extends Reader[Message] {
    override def read(update: JValue): Message = {
      val id = (update \ "update_id").extract[Int]
      val msg = update \ "message"


      val text: String = (msg \ "text").extract[String]
      val chat: Long = (msg \ "chat" \ "id").extract[Long]
      val user: String = (msg \ "from" \ "first_name").extract[String]

      Message(id = id,
        text = text,
        chat = chat,
        user = user)
    }
  }
}
