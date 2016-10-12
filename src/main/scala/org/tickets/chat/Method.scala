package org.tickets.chat

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.Reader
import org.tickets.chat.Telegram.{BotToken, TgMethod, TgReq}
import org.tickets.uz.Station

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
case object GetUpdates extends Method with Json4sSupport{

  def byOffset(offset: Int, token: BotToken): TgReq =
    RequestBuilding.Post(token.GetUpdatesUri, Map("offset" -> offset)) -> GetUpdates

  def withoutOffset(token: BotToken): TgReq =
    RequestBuilding.Post(token.GetUpdatesUri) -> GetUpdates
}
