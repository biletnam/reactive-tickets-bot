package com.github.bsnisar.tickets.telegram

import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.talk.TgResponses.Reply
import com.github.bsnisar.tickets.telegram.TelegramMessages.Updates
import org.json4s.{JValue, Reader}

import scala.concurrent.Future

object TelegramMessages {

  case class Updates(lastSeq: Int, data: Iterable[Update])



  trait Update {
    def seqNum: Int
    def text: String
    def chat: String

    def mkReply(msg: Msg): Reply = Reply(chat, msg)
  }



  /**
    * Simple Telegram Update message.
    * @param seqNum single update 'update_id' field
    * @param text update's 'text' field
    * @param chat update's chat id
    */
  final case class TgUpdate(seqNum: Int, text: String, chat: String) extends Update

  object Update {
    implicit object Reader extends Reader[Update] with Json {
      override def read(value: JValue): Update = {
        val id = (value \ "update_id").as[Int]
        val msg = value \ "message"
        val text = msg \ "text"
        val chatID = "0"

        TgUpdate(id, text.as[String], chatID)
      }
    }

    /**
      * Extracting text from update.
      */
    object Text {
      def unapply(arg: Update): Option[String] = Option(arg.text)
    }

  }
}

trait TelegramMessages {

  /**
    * Pull updates from Telegram.
    * @param offset offset
    * @return updates.
    */
  def pull(offset: Int): Future[Updates]
}