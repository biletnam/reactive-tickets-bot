package com.github.bsnisar.tickets.telegram.actor

import akka.actor.{Actor, Props}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.{Json, Templates}
import com.github.bsnisar.tickets.telegram.{Msg, TelegramUpdates}
import com.github.bsnisar.tickets.wire.Wire

object TelegramPush {
  def props(wire: Wire[Req, _], t: Templates)
           (implicit m: Materializer): Props = Props(classOf[TelegramPush], wire, t, m)
}

class TelegramPush(val wire: Wire[Req, _], val template: Templates)(implicit m: Materializer) extends Actor with Json {
  import org.json4s.JsonDSL._
  import context.dispatcher

  override def receive: Receive = {
    case TelegramUpdates.Reply(chat, msg) =>
      doRequest(chat, msg)
  }

  private def doRequest(chat: String, msg: Msg) = {
    val payload = template.eval(msg)
    val json =
      ("chat_id" -> chat) ~
        ("text" -> payload)

    val req: HttpRequest = RequestBuilding.Post("/sendMessage", json)
    Source.single(req -> 1)
      .via(wire.flow)
      .runWith(Sink.head)
  }


}
