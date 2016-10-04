package org.tickets.uz.cmd

import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import org.tickets.misc.ActorSlf4j

import scala.concurrent.ExecutionContext

/**
  * Handle success responses and delegate and log failed.
  * @param delegate delegate
  * @author bsnisar
  */
class CmdSuccessStatus(val delegate: Command) extends Command with ActorSlf4j {

  override def exec(httpResponse: HttpResponse)(implicit mt: Materializer, ec: ExecutionContext): Unit = {
    httpResponse.status match {
      case code if code.isSuccess() =>
        delegate.exec(httpResponse)
      case code =>
        log.error("Response not succeed {}", code)
    }
  }
}
