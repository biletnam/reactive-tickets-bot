package org.tickets.telegram

import akka.actor.Actor
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Push.PushMsg
import org.tickets.telegram.Telegram.{BotToken, HttpFlow}


object Push {
  import org.json4s.JsonDSL._

  type PushMsg = Msg

  /**
    * Telegram message with content
    */
  sealed trait Msg {
    def toJson: JValue
  }

  case class TextMsg(chatId: Long, text: String) extends Msg {
    override def toJson: JValue =
      ("chat_id" -> chatId) ~
      ("text" -> text)
  }

}

class Push(httpFlow: HttpFlow, botToken: BotToken, mt: Materializer) extends Actor with LogSlf4j {
  override def receive: Receive = push()

  import context.dispatcher
  implicit val materializer = mt

  def push(): Receive = {
    case msg: PushMsg =>
      pushMsg(msg).onFailure {
        case ex => log.error("#push failed", ex)
      }
  }

  def pushMsg(msg: PushMsg) =
    Source.single(SendMessage(msg.toJson, botToken))
      .via(httpFlow)
      .runWith(Sink.head)


}
