package org.tikets.misc

import org.slf4j.{Logger, LoggerFactory}

/**
  * Log.
  */
trait Log {
  val log = LoggerFactory.getLogger(this.getClass)
}
