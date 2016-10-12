package org.tickets.misc

import org.slf4j.LoggerFactory

/**
  * Created by bsnisar on 13.10.16.
  */
object Log {
  private val Log = LoggerFactory.getLogger("Log")

  def logMessage(json: String): Unit = {
    Log.debug("Get content {}", json)
  }

}
