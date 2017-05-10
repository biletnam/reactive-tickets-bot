package com.github.bsnisar.tickets.wire

import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import com.github.bsnisar.tickets.Ws.{Req, Res}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}


class LogWire(val origin: Wire[Req, Res], val prefix: String = "")(implicit m: Materializer, ex: ExecutionContext)
  extends Wire[Req, Res] with LazyLogging {
  override def flow: Flow[Req, Res, _] = loggerFlow

  private lazy val loggerFlow: Flow[Req, Res, _] = Flow[Req]
    .via(loggingRequestFlow)
    .via(origin.flow)
    .via(loggingResponseFlow)

  private lazy val loggingResponseFlow = Flow[Res].map { resp =>
    if (logger.underlying.isDebugEnabled) {
      logResponse(resp._1)
    }

    resp
  }

  private lazy val loggingRequestFlow = Flow[Req].map { req =>
    if (logger.underlying.isDebugEnabled) {
      logRequest(req._1)
    }

    req
  }

  private def logResponse(maybeResponse: Try[HttpResponse])
                    (implicit m: Materializer, ex: ExecutionContext): Unit = maybeResponse match {
    case Success(resp) =>
      val entity = resp.entity
      val charsetValue = entity.contentType
        .charsetOption
        .getOrElse(HttpCharsets.`UTF-8`)
        .value

      val asyncContent = entity.dataBytes
        .map(_.decodeString(charsetValue))
        .runWith(Sink.head)

      asyncContent.onComplete {
        case Success(body) =>
          logger.debug(s"[$prefix] [RESP] Status: ${resp.status}, Headers ${resp.headers}, Content: $body")
        case Failure(error) =>
          logger.error(s"[$prefix] [RESP] Status: ${resp.status}, Headers ${resp.headers}, Content failed", error)
      }

    case Failure(error) =>
      logger.error(s"[$prefix] [RESP] HttpResponse failed", error)
  }

  private def logRequest(req: HttpRequest): Unit = {
    logger.debug(s"[$prefix] [REQ] Method: ${req.method}, Uri: ${req.uri}, Headers: ${req.headers}")
  }
}
