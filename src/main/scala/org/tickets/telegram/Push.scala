package org.tickets.telegram

import akka.actor.{Actor, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Push.PushMsg
import org.tickets.telegram.Telegram.HttpFlow


object Push {
  import org.json4s.JsonDSL._

  def props(httpFlow: HttpFlow, botToken: MethodBindings)(implicit mt: Materializer): Props =
    Props(classOf[Push], httpFlow, botToken, mt)

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
class Push(httpFlow: HttpFlow, botToken: MethodBindings, mt: Materializer) extends Actor with LogSlf4j {
  override def receive: Receive = push()

  import context.dispatcher
  implicit val materializer = mt

  private def push(): Receive = {
    case msg: PushMsg =>
      log.trace("#push: sending message {}", msg)
      pushMsg(msg).onFailure {
        case ex => log.error("#push failed", ex)
      }
  }

  private def pushMsg(msg: PushMsg) =
    Source.single(botToken.createSendMessage(msg))
      .via(httpFlow)
      .runWith(Sink.head)


}
