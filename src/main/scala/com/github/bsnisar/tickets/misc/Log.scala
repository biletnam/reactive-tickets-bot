package com.github.bsnisar.tickets.misc

import org.slf4j.{Logger, LoggerFactory}

/**
  * Logger capabilities.
  *
  * @author bsnisar
  */
trait Log {
 val log: Logger = LoggerFactory.getLogger(this.getClass)
}
