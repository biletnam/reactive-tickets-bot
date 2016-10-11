package org.tickets.chat

import akka.http.scaladsl.model.HttpRequest

object Telegram {
  type Req = (HttpRequest, Bound)
  type Bound =  ReqType

  trait ReqType
  case object Updates extends ReqType

}
