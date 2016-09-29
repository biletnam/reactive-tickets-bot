package org.tickets.bot.tg

trait TelegramRequest {
  def toMap: Map[String, Any]
}


case class Test(text: String) extends TelegramRequest {

  override def toMap: Map[String, Any] = ???
}
