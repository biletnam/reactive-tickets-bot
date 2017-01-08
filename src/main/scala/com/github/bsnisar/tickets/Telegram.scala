package com.github.bsnisar.tickets

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
  def pull(offset: Int = 0): Future[Iterable[Any]]
}
