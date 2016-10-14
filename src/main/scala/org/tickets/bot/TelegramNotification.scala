package org.tickets.bot

import java.text.MessageFormat
import java.util.ResourceBundle

import akka.actor.ActorRef
import org.tickets.bot.TelegramNotification.Code
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
    * push bundle message to chat
    * @param msg id of bundle
    */
  def pushCode(msg: Code): Unit = push(TelegramNotification.Bundle.getString(msg))

  /**
    * push bundle message to chat with argument for placeholder
    * @param msg bundle id
    * @param arg argument
    */
  def pushCode(msg: Code, arg: AnyRef): Unit = push(MessageFormat.format(
    TelegramNotification.Bundle.getString(msg), arg))


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

object TelegramNotification {
  lazy val Bundle = ResourceBundle.getBundle("Messages")


  final type Code = String
  val RailwayApiError: Code = "ask.error.railway.sys"
  val StationsNotFound: Code = "ask.stations.not.found"
}

/**
  * Wrapper for telegram ref, that handle logic of sending appropriate messages.
  * @param chatId chat id
  * @param telegramRef [[ActorRef]] to particular Telegram API
  */
final class NotifierRef(val chatId: ChatId, val telegramRef: ActorRef) extends TelegramNotification