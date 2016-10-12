package org.tickets.chat

import akka.actor.Actor
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.tickets.chat.Telegram.{BotToken, TgReq, TgRes}
import org.json4s.JValue
import de.heikoseeberger.akkahttpjson4s.Json4sSupport

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class UpdatesStreamer(flow: Flow[TgReq, TgRes, _], token: BotToken) extends Actor with Json4sSupport {
  override def receive: Receive = ???

  private var offset = -1

  def pulling(): Receive = {
    case 42 =>

  }

  def readUpdates =
    Source.single(GetUpdates.byOffset(offset,token))
      .via(flow)
        .via(Flow[TgRes].map {
          case (Success(httpResponse), method) => parseUpdates(httpResponse)
        })


  def parseUpdates(res: HttpResponse): Update = {
    val json = Await.result(Unmarshal(res.entity).to[JValue], 10.seconds)
    new UpdateJs(json)
  }


}
