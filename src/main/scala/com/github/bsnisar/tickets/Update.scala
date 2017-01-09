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
    * Chat room ID.
    * @return chat room id.
    */
  def chat: Long

  /**
    * Message text.
    * @return msg text.
    */
  def text: String
}
