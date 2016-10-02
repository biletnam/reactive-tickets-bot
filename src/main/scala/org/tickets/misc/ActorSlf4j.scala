package org.tickets.misc

import org.slf4j.LoggerFactory

/**
  * Log.
  */
trait ActorSlf4j {
  val log = LoggerFactory.getLogger(this.getClass)
}
