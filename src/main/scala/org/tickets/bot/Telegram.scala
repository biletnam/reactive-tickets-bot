package org.tickets.bot

import java.util.{Locale, ResourceBundle}

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import org.tickets.api.{Msg, TextMsg}
import org.tickets.bot.Telegram.{PollUpdates, PushMsg, SendMessage, TelegramMethod}

import scala.concurrent.Future
import scala.util.Try

/**
  * Telegram API.
  */
class Telegram(val telegramApiFlow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), _] ) extends Actor {

  override def receive: Receive = {
    case PushMsg(code, chatId, local) =>
      val msg = ResourceBundle.getBundle("Bot", local).getString(code)
      sendRequest(TextMsg(chat = chatId, text = msg), SendMessage)
    case PollUpdates =>

  }


  def sendRequest(msg: Msg, tgMethod: TelegramMethod): Future[(Try[HttpResponse], Int)] = {
    val post: HttpRequest = RequestBuilding.Post(tgMethod.url, msg)
    Source.single(post -> 1).via(telegramApiFlow).runWith(Sink.head)
  }
}

object Telegram {
  lazy val getMe = "/getMe"
  lazy val getUpdates = "/getUpdates"
  lazy val sendMessage = "/sendMessage"

  /**
    * Telegram API method.
    */
  type TelegramMethod = TgMethod

  /**
    * Generic method.
    */
  sealed trait TgMethod {
    def url: String
  }

  case object GetMe extends TgMethod {
    override def url: String = "/getMe"
  }

  /**
    * Telegram API:
    * <a href="https://core.telegram.org/bots/api#sendmessage">SendMessage</a> method.
    */
  case object SendMessage extends TgMethod {
    override def url: String = "/sendMessage"
  }

  /**
    * HTTPS host based flow. Use bot.api.host value from configuration.
    * @param cfg app config
    * @return https flow
    */
  def https(cfg: Config): Flow[(HttpRequest, Int), (Try[HttpResponse], Int), _] = {
    Http().newHostConnectionPoolHttps[Int](cfg.getString("bot.api.host"))
  }

  case object PollUpdates

  /**
    * Send message to client.
    * @param msgCode l19n code
    * @param local locale
    */
  case class PushMsg(msgCode: String, chatId: Long, local: Locale = Locale.ENGLISH)

}