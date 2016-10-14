package org.tickets.telegram

import akka.actor.{Actor, Props}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Method.{TgMethod, TgReq}
import org.tickets.telegram.TelegramPush.PushMsg
import org.tickets.telegram.Telegram.HttpFlow

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


object TelegramPush {
  import org.json4s.JsonDSL._

  def props(httpFlow: HttpFlow, botToken: MethodBindings)(implicit mt: Materializer): Props =
    Props(classOf[TelegramPush], httpFlow, botToken, mt)

  type PushMsg = Msg

  /**
    * Telegram message with content
    */
  sealed trait Msg {
    def toJson: JValue
  }

  /**
    * Send text to some chat.
    * @param chatId chat id
    * @param text text to client
    */
  case class TextMsg(chatId: Long, text: String) extends Msg {
    override def toJson: JValue =
      ("chat_id" -> chatId) ~
      ("text" -> text)
  }

}

/**
  * Push message to Telegram API
  * @param httpFlow connection to Telegram API host
  * @param botToken bot token
  * @param mt materializer
  */
class TelegramPush(httpFlow: HttpFlow, botToken: MethodBindings, mt: Materializer) extends Actor with LogSlf4j {
  override def receive: Receive = push()

  import context.dispatcher
  implicit val materializer = mt

  private def push(): Receive = {
    case msg: PushMsg =>
      val push: Future[(Try[HttpResponse], TgMethod)] = pushMsg(msg)
      push.onSuccess {
        case (Success(resp: HttpResponse), e) if resp.status.isFailure() =>
          val msg: String = Await.result(Unmarshal(resp.entity).to[String], 1.second)
          log.warn("[#push] req {} failed {} {}",
            e, resp.status.value, msg)
        case (Failure(ex), _) => log.error("#push failed", ex)
      }

      push.onFailure {
        case ex => log.error("send failed", ex)
      }
  }

  private def pushMsg(msg: PushMsg) = {
    val message: TgReq = botToken.createSendMessage(msg)
    Source.single(message)
      .via(httpFlow)
      .runWith(Sink.head)
  }
}
