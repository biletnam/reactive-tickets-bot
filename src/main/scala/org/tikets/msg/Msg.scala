package org.tikets.msg

trait Msg {

  /**
    * Message identification
    * @return id
    */
  def id: ID

  /**
    * text fields from getUpdates Telegram API call.
    * @return user input as text
    */
  def text: String

  /**
    * Phrase from input.
    * @return phrase from text.
    */
  def phrase: Phrase
}


/**
  * User and and chat id
  * @param user user
  * @param chat chat
  */
case class ID(user: String, chat: Long)
