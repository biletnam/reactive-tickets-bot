package org.tickets.chat

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
    * Text message from Telegram
    * @return string
    */
  def text: String
}

case class UpdateJs(id: Int, msg: JValue) extends Update {

  def this(up: JValue) = {
    this((up \ "update_id").extract[Int], up \ "message")
  }

  override def text: String = (msg \ "text").extract[String]
}
