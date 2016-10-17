package org.tickets.misc

import akka.http.scaladsl.model.StatusCode

/**
  * Created by bsnisar on 14.10.16.
  */
class HttpProtocolException(status: StatusCode) extends RuntimeException(s"api respond with $status")


