package org.tickets.telegram

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.tickets.misc.{LogSlf4j, Logger}
import org.tickets.telegram.Method.TgReq
import org.tickets.telegram.TelegramPush.Msg

import scala.concurrent.{Await, ExecutionContext}
import scala.util.Try

object Method {
  type TgMethod = Method
  type TgReq = (HttpRequest, TgMethod)
  type TgRes = (Try[HttpResponse], TgMethod)
}

final class MethodBindings(value: String) extends Json4sSupport with LogSlf4j {
  import org.tickets.misc.JsonSupport._
  private lazy val GetUpdatesUri: Uri = Uri(s"/bot$value/getUpdates")
  private lazy val SendMessageUri: Uri = Uri(s"/bot$value/sendMessage")

  /**
    * Create request type: <a href="https://core.telegram.org/bots/api#getupdates">getUpdates</a>
    * @param offset updates offset
    * @return [[TgReq]]
    */
  def createGetUpdates(offset: Option[Int] = None)(implicit ex: ExecutionContext): TgReq = {
    val httpRequest =
      if (offset.isEmpty)
        RequestBuilding.Post(GetUpdatesUri)
      else
        RequestBuilding.Post(GetUpdatesUri, Map("offset" -> offset.get))

    httpRequest -> GetUpdates
  }

  /**
    * Create request type: <a href="https://core.telegram.org/bots/api#getupdates">sendMessage</a>
    * @param content body for sending
    * @param ex executor for marshaller
    * @return telegram request
    */
  def createSendMessage(content: Msg)(implicit ex: ExecutionContext): TgReq = {
    import scala.concurrent.duration._
    val entity = Await.result(Marshal(content.toJson).to[RequestEntity], 1.second)
    log.trace("createSendMessage: {}", entity)
    RequestBuilding.Post(SendMessageUri, entity) -> SendMessage
  }

}

object MethodBindings {
  def apply(token: String): MethodBindings = {
    Logger.Log.info("MethodBindings( telegram bot token = {} )", token)
    new MethodBindings(token)
  }
}

/**
  * Telegram requests method type.
  *
  * @author bsnisar
  */
trait Method

/**
  * Request type: <a href="https://core.telegram.org/bots/api#getupdates">getUpdates</a>
  * @author bsnisar
  */
case object GetUpdates extends Method

case object SendMessage extends Method