package org.tickets.uz.cmd

import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import org.json4s.JsonAST.JBool
import org.json4s._
import org.tickets.misc.Req

import scala.concurrent.ExecutionContext

/**
  * Command on result of remote api call.
  */
trait Command extends Req {

  /**
    * Execute command on given http response.
    * @param httpResponse response
    */
  def exec(httpResponse: HttpResponse)(implicit mt: Materializer, ec: ExecutionContext): Unit
}


/**
  * Generic command for UZ API.
  *
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
