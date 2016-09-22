package org.tickets.bot

import java.util.{Locale, ResourceBundle}

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{Formats, JValue, Serialization}
import org.tickets.api.{Msg, TextMsg}
import org.tickets.bot.Telegram._
import org.tickets.misc.Log

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Telegram API.
  */
class Telegram(val telegramApiFlow: Flow[(HttpRequest, TelegramMethod), (Try[HttpResponse], TelegramMethod), _] )
  extends Actor with Log with Json4sSupport {

  import akka.http.scaladsl.unmarshalling._
  import context.dispatcher
  implicit val mt: Materializer = null
  implicit val sz: Serialization = null
  implicit val fs: Formats = null

  override def receive: Receive = {
//    case PushMsg(code, chatId, local) =>
//      val msg = ResourceBundle.getBundle("Bot", local).getString(code)
//      sendRequest(TextMsg(chat = chatId, text = msg), SendMessage)
    case PollUpdates =>
      getRequest(GetUpdates) onSuccess {
        case (Success(resp), _) =>
          val values = Unmarshal(resp.entity).to[JValue]
      }
  }

//  def sendRequest(msg: Msg, tgMethod: TelegramMethod)(implicit ec: ExecutionContext): Future[(Try[HttpResponse], TelegramMethod)] = {
//    val post: HttpRequest = RequestBuilding.Post(tgMethod.url, msg)
//    Source.single(post -> tgMethod).via(telegramApiFlow).runWith(Sink.head)
//  }

  def getRequest(tgMethod: TelegramMethod)(implicit ec: ExecutionContext): Future[(Try[HttpResponse], TelegramMethod)] = {
    val post: HttpRequest = RequestBuilding.Get(tgMethod.url)
    Source.single(post -> tgMethod).via(telegramApiFlow).runWith(Sink.head)
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

  case object GetUpdates extends TgMethod {
    override def url: String = getUpdates
  }

  /**
    * HTTPS host based flow. Use bot.api.host value from configuration.
    * @param cfg app config
    * @return https flow
    */
  def https(cfg: Config)(implicit ac: ActorSystem, mt: Materializer): Flow[(HttpRequest, Int), (Try[HttpResponse], Int), _] = {
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