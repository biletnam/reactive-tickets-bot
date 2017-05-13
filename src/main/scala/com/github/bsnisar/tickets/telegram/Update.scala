package com.github.bsnisar.tickets.telegram

import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.talk.Answers.{Answer, AnswerBean}
import org.json4s.{JValue, Reader}

trait Update {
  /**
    * update_id field from <a href="https://core.telegram.org/bots/api#update">Update</a> object.
    * @return
    */
  def seqNum: Int

  /**
    * text field from <a href="https://core.telegram.org/bots/api#message">Message</a> object.
    * @return
    */
  def text: String

  /**
    * id field from <a href="https://core.telegram.org/bots/api#chat">Chat</a> object.
    * @return
    */
  def chat: String

  def createAnswer(msg: Msg): Answer = AnswerBean(chat, msg)
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

      val msgJson = value \ "message"
      val chatJson = msgJson \ "chat"

      val text = (msgJson \ "text").as[String]
      val chatID = (chatJson \ "id").as[String]

      TgUpdate(id, text, chatID)
    }
  }


  /**
    * Extracting text from update.
    */
  object Text {
    def unapply(arg: Update): Option[String] = Option(arg.text)
  }
}
