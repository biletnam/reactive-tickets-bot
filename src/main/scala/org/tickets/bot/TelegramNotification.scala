package org.tickets.bot

import akka.actor.ActorRef
import org.tickets.telegram.Telegram.ChatId
import org.tickets.telegram.TelegramPush
import org.tickets.telegram.TelegramPush.Msg

/**
  * Decorator for telegram actor ref, that add additional work for wrapping messages.
  * @author bsnisar
  */
trait TelegramNotification {

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
    * push raw string to chat
    * @param msg string
    */
  def push(msg: String): Unit = telegramRef ! TelegramPush.TextMsg(chatId, msg)

  /**
    * Push particular message
    * @param msg [[Msg]] object
    */
  def push(msg: Msg): Unit = telegramRef ! msg

  /**
    * Analog of push method
    * @param msg string
    */
  def <<(msg: String) = push(msg)
}

/**
  * Wrapper for telegram ref, that handle logic of sending appropriate messages.
  * @param chatId chat id
  * @param telegramRef [[ActorRef]] to particular Telegram API
  */
case class NotifierRef(chatId: ChatId, telegramRef: ActorRef) extends TelegramNotification