package com.github.bsnisar.tickets.telegram

import com.github.bsnisar.tickets.misc.Json
import org.json4s.{JValue, Reader}

/**
  * Telegram Update message.
  * @param id single update 'update_id' field
  * @param text update's 'text' field
  * @param chat update's chat id
  */
final case class TgUpdate(id: Int, text: String, chat: String)

object TgUpdate {
  implicit object Reader extends Reader[TgUpdate] with Json {
    override def read(value: JValue): TgUpdate = {
      val id = (value \ "update_id").as[Int]
      val msg = value \ "message"
      val text = msg \ "text"
      val chatID = "0"

      TgUpdate(id, text.as[String], chatID)
    }
  }
}

