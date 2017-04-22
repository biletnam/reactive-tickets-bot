package com.github.bsnisar.tickets.telegram

import scala.concurrent.Future

/**
  * Telegram.
  */
trait Telegram {

  /**
    * Push message to chat.
    *
    * @param chatId chat id
    * @param msg message
    */
  def push(chatId: Long, msg: String): Unit

  /**
    * Pull for updates.
    *
    * @return updates.
    */
  def pull(offset: Int): Future[Iterable[TgUpdate]] = Future.successful(List.empty[TgUpdate])

  /**
    * Information about bot.
    * @return information string
    */
  def info: Future[String]
}
