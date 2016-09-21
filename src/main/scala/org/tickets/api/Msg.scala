package org.tickets.api

import org.json4s.JsonAST.{JInt, JLong, JObject, JValue}

/**
  * Created by bsnisar on 21.09.16.
  */
trait Msg {
  def json: JValue
}

trait ReplyMarkup extends Msg



case class TextMsg(chat: Long, text: String, markups: List[ReplyMarkup] = Nil) extends Msg {
  import org.json4s.JsonDSL._
  import org.json4s._

  override def json: JValue = {
    ("chat_id" -> chat) ~ ("text" -> text)
  }
}