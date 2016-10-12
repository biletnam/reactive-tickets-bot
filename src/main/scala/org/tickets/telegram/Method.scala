package org.tickets.telegram

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, HttpResponse}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JValue
import org.tickets.telegram.Method.TgReq
import org.tickets.telegram.Telegram.BotToken

import scala.concurrent.ExecutionContext
import scala.util.Try

object Method {
  type TgMethod = Method
  type TgReq = (HttpRequest, TgMethod)
  type TgRes = (Try[HttpResponse], TgMethod)
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
case object GetUpdates extends Method with Json4sSupport {
  import org.tickets.misc.JsonUtil._

  def apply(offset: Int, token: BotToken)(implicit ex: ExecutionContext): TgReq =
    RequestBuilding.Post(token.GetUpdatesUri, Map("offset" -> offset)) -> GetUpdates

  def apply(token: BotToken): TgReq =
    RequestBuilding.Post(token.GetUpdatesUri) -> GetUpdates
}

case object SendMessage extends Method with Json4sSupport {
  import org.tickets.misc.JsonUtil._

  def apply(content: JValue, token: BotToken)(implicit ex: ExecutionContext): TgReq = {
    val body = Marshal(content).to[HttpEntity]
    RequestBuilding.Post(token.SendMessageUri, body) -> SendMessage
  }

}