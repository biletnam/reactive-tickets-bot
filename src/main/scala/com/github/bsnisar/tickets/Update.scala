package com.github.bsnisar.tickets

/**
  * Telegram updates.
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
