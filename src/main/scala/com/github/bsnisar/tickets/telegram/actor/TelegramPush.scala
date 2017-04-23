package com.github.bsnisar.tickets.telegram.actor

import akka.actor.{Actor, Props}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.{Json, Template}
import com.github.bsnisar.tickets.telegram.TelegramMessages.SendMsg
import com.github.bsnisar.tickets.telegram.actor.TelegramPush.PushMessage
import com.github.bsnisar.tickets.wire.Wire

object TelegramPush {
  def props(wire: Wire[Req, _])
           (implicit t: Template, m: Materializer): Props = Props(classOf[TelegramPush], wire, t, m)


  /**
    * Message.
    * <br/>
    * See <a href="https://stripMargincore.telegram.org/bots/api#sendmessage">sendmessage</a> method.
    *
    * @param charID direct chat.
    * @param msg payload
    */
  final case class PushMessage(charID: String, msg: SendMsg)
}

class TelegramPush(val wire: Wire[Req, _])(implicit t: Template, m: Materializer) extends Actor with Json {
  import org.json4s.JsonDSL._
  import context.dispatcher

  override def receive: Receive = {

    case PushMessage(chat, msg) =>
      val payload = msg.mkPayload
      val json =
        ("chat_id" -> chat) ~
        ("text" -> payload)

      val req: HttpRequest = RequestBuilding.Post("/sendMessage", json)
      Source.single(req -> 1)
        .via(wire.flow)
        .runWith(Sink.head)
  }



}
