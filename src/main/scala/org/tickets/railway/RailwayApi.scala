package org.tickets.railway

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream._
import akka.stream.scaladsl.Flow
import org.tickets.misc.{HttpProtocolException, LogSlf4j}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object RailwayApi extends LogSlf4j {

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

  private def handle(response: Try[HttpResponse]): Any = response match {
    case Success(httpResponse) if httpResponse.status.isSuccess() =>
//      val stations = Unmarshal(httpResponse.entity).to[JValue].map(toStations)
      ???
    case Success(httpResponse) if !httpResponse.status.isSuccess() =>
      log.warn("api respond by not success status {}", httpResponse.status.value)
      Future.failed(new HttpProtocolException(httpResponse.status))
    case Failure(err) =>
      log.error("request send failed", err)
      Future.failed(err)
  }


  private def onHttpResponse[E](handler: HttpResponse => Future[E]) =
    Flow[Res].map {
      case (Success(httpResp), _) if httpResp.status.isSuccess() =>
        handler(httpResp)
      case (Success(httpResp), _) if !httpResp.status.isSuccess() =>
        log.warn("api respond by not success status {}", httpResp.status.value)
        Future.failed[E](new HttpProtocolException(httpResp.status))
      case (Failure(err), _)=>
        log.error("request send failed", err)
        Future.failed[E](err)
    }


}