package com.github.bsnisar.tickets

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
    * Updates for bot.
    * @return updates.
    */
  def updates: Updates
}
