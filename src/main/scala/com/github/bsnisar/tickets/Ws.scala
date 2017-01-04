package com.github.bsnisar.tickets

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Flow
import org.json4s._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Ws {
  type Task = Any
  type Req = (HttpRequest, Task)
  type Res = (Try[HttpResponse], Task)
  type HttpFlow = Flow[Req, Res, Any]


  /**
    *
    * @return
    */
  def asJSON: PartialFunction[Res, Future[JValue]] =  {
      case (Success(httpResp), _) if httpResp.status.isSuccess() =>
        Unmarshal(httpResp.entity).to[JValue]
      case (Success(httpResp), _) if !httpResp.status.isSuccess() =>
        log.warn("api respond by not success status {}", httpResp.status.value)
        throw new IllegalStateException(s"${httpResp.status}")
      case (Failure(err), _) =>
        log.error("request send failed", err)
        throw err
  }

}
