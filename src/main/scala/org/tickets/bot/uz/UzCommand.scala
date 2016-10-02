package org.tickets.bot.uz

import org.json4s._

/**
  * Generic command for UZ API.
  * @author Bogdan_Snisar
  */
trait UzCommand {

  /**
    * Common behavior for UZ api responses.
    * @param json response content
    */
  def parseUzAnswer(json: JValue): Unit = json \ "error" match {
    case JBool.True => onApiError(json)
    case _ => onContent(json \ "value")
  }

  /**
    * Callback on bossiness content of API response
    * @param data data
    */
  def onContent(data: JValue): Unit

  /**
    * API response error message.
    * @param json full content
    */
  def onApiError(json: JValue) = System.err.println("api error: " + json)
}

