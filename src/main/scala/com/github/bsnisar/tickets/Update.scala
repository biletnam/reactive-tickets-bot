package com.github.bsnisar.tickets

/**
  * Telegram update message.
  */
trait Update {
  /**
    * Update sequence id.
    * @return id
    */
  def id: Int

  /**
    * Message text.
    * @return msg text.
    */
  def text: String
}
