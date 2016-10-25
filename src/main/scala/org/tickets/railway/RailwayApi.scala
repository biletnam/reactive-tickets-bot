package org.tickets.railway

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream._
import akka.stream.scaladsl.Flow
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JValue
import org.tickets.misc.{HttpProtocolException, LogSlf4j}
import org.tickets.misc.JsonSupport._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object RailwayApi extends LogSlf4j with Json4sSupport {

  type Task = Any
  type Req = (HttpRequest, Task)
  type Res = (Try[HttpResponse], Task)

  type ApiFlow = Flow[Req, Res, Any]

  /**
    * Http Pool for UZ API.
    * @return flow for it
    */
  def httpFlowUzApi(implicit as: ActorSystem, mt: Materializer): ApiFlow = {
    Http().newHostConnectionPool[Task]("booking.uz.gov.ua")
  }

  def httpRespFlow[E](handler: HttpResponse => E) = Flow[Res].map {
      case (Success(httpResp), _) if httpResp.status.isSuccess() =>
        handler(httpResp)
      case (Success(httpResp), _) if !httpResp.status.isSuccess() =>
        log.warn("api respond by not success status {}", httpResp.status.value)
        throw new HttpProtocolException(httpResp.status)
      case (Failure(err), _)=>
        log.error("request send failed", err)
        throw err
    }


  def asJSON(r: HttpResponse)(implicit ex: ExecutionContext, mt: Materializer): Future[JValue] =
    Unmarshal(r.entity).to[JValue]

  def mapHttpResponse[E](handler: HttpResponse => Future[E]) = Flow[Res].mapAsync(10) {
    case (Success(httpResp), _) if httpResp.status.isSuccess() =>
      handler(httpResp)
    case (Success(httpResp), _) if !httpResp.status.isSuccess() =>
      log.warn("api respond by not success status {}", httpResp.status.value)
      Future.failed(new HttpProtocolException(httpResp.status))
    case (Failure(err), _)=>
      log.error("request send failed", err)
      Future.failed(err)
  }
}