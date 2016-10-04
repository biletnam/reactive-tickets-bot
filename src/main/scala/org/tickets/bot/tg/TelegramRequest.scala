package org.tickets.bot.tg

trait TelegramRequest {
  def toMap: Map[String, Any]
}



