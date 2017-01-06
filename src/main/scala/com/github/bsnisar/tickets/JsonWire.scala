package com.github.bsnisar.tickets

import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.{Req, Res}
import org.json4s.JValue

import scala.util.{Failure, Success}


class JsonWire(private val origin: Wire[Req, Res], private val mt: Materializer) extends Wire[Req, JValue] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def flow: Flow[Req, JValue, _] = {
    Flow[Req].via(origin.flow).via(asJSON)
  }

  /**
    * Parse response as json.
    * @return new flow.
    */
  private def asJSON(implicit mt: Materializer): Flow[Res, JValue, Any]  = Flow[Res].mapAsync(1) {
    case (Success(httpResp), _) if httpResp.status.isSuccess() =>
      Unmarshal(httpResp.entity).to[JValue]
    case (Success(httpResp), _) if !httpResp.status.isSuccess() =>
      throw new IllegalStateException(s"${httpResp.status}")
    case (Failure(err), _) =>
      throw err
  }
}
