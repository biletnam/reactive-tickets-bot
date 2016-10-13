package org.tickets.telegram

import org.json4s.JValue
import org.json4s._
import org.tickets.misc.HttpSupport.Json4sImplicits._

trait Update {

  /**
    * Id of update msg.
    * @return id
    */
  def id: Int

  /**
    * Chat id
    * @return id of bot chat
    */
  def chat: Long

  /**
    * Client username
    * @return username
    */
  def user: String

  /**
    * Text message from Telegram
    * @return string
    */
  def text: String
}

case class UpdateJVal(id: Int, msg: JValue) extends Update {

  def this(up: JValue) = {
    this((up \ "update_id").extract[Int], up \ "message")
  }

  override def text: String = (msg \ "text").extract[String]
  override def chat: Long = (msg \ "chat" \ "id").extract[Long]
  override def user: String = (msg \ "from" \ "first_name").extract[String]
}
