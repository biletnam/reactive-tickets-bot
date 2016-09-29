package org.tickets.misc

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.util.Try

/**
  * Created by bsnisar on 29.09.16.
  */
object HttpSupport {

  type Request = (HttpRequest, Int)
  type Response = (Try[HttpResponse], Int)

}
