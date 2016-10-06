package org.tickets.misc

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import org.json4s.{DefaultFormats, Formats, Serialization, jackson}

import scala.util.Try

/**
  * Created by bsnisar on 29.09.16.
  */
object HttpSupport extends ActorSlf4j {
  type Request = (HttpRequest, Bound)
  type Response = (Try[HttpResponse], Bound)

  type Bound = Req

  /**
    * Implicit default serialization and formats
    */
  object Json4sImplicits {
    implicit val fs: Formats = DefaultFormats
    implicit val sz: Serialization = jackson.Serialization
  }
}


/**
  * Context of http request.
  *
  * @author bsnisar
  */
trait Req