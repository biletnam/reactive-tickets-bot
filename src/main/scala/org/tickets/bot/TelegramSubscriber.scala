package org.tickets.bot


import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JsonAST.{JArray, JBool}
import org.json4s.{DefaultFormats, Formats, JValue, Serialization}
import org.tickets.misc.ActorSlf4j

import scala.concurrent.Await
import scala.util.Success
import scala.concurrent.duration._

object TelegramSubscriber {
  def props(chatBot: ActorRef, mt: Materializer): Props = Props(classOf[TelegramSubscriber], chatBot, mt)
}

class TelegramSubscriber(chatBot: ActorRef, mt: Materializer) extends ActorSubscriber with Json4sSupport
  with ActorSlf4j {

  override protected def requestStrategy: RequestStrategy =
    WatermarkRequestStrategy(100)

  private implicit val materializer = mt
  private implicit val format: Formats = DefaultFormats
  private implicit val serialization: Serialization = org.json4s.jackson.Serialization

  import context.dispatcher

  override def receive: Receive = {
    case OnNext((Success(httpResp: HttpResponse), method)) =>
      val promiseJson = Unmarshal(httpResp.entity).to[JValue]
      val json: JValue = Await.result(promiseJson, 10.seconds)
      log.debug("received telegram json {}", json)
      handle(json)

    case e @ _ =>
      log.debug("get unhandled msg {}", e)
  }


  private def handle(json: JValue): Unit = {
    json \ "ok" match {
      case JBool.True =>
        parseMsg(json)

      case JBool.False =>
        log.warn("telegram api error {}", json)

      case e @ _ =>
        log.warn("for unhandled {}", json)
    }
  }


  private def parseMsg(json: JValue): Unit = {
    json \ "result" match {
      case JArray(messages) =>
        messages foreach (chatBot ! _)
      case e @ _ =>
        log.debug("Json unhandled. Expect {'result': [..]}, but {}", json)

    }
  }
}
