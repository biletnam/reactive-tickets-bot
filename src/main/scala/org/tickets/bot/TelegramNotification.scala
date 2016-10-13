package org.tickets.bot

import akka.actor.ActorRef
import org.tickets.telegram.Telegram.ChatId
import org.tickets.telegram.TelegramPush

trait TelegramNotification {

  trait Code {
    def value: String
  }

  /**
    * Target Telegram API
    * @return ref
    */
  def telegramRef: ActorRef

  /**
    * Chat id for pushing.
    * @return chat id
    */
  def chatId: ChatId

  /**
    * push bundle message to chat
    * @param msg id of bundle
    */
  def pushCode(msg: Code): Unit = ???

  /**
    * push raw string to chat
    * @param msg string
    */
  def push(msg: String): Unit = telegramRef ! TelegramPush.TextMsg(chatId, msg)

  /**
    * Analog of push method
    * @param msg string
    */
  def <<(msg: String) = push(msg)
}

class Notifier(val chatId: ChatId, val telegramRef: ActorRef) extends TelegramNotification