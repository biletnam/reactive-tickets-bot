package org.tickets.misc

import org.slf4j.{LoggerFactory, Marker, MarkerFactory}

object Logger {
  private val Marker: Marker = MarkerFactory.getMarker("content")
  private val Log = LoggerFactory.getLogger(this.getClass)


  def logMessage(json: String): Unit = {
    Log.debug(Marker,"Get content {}", json)
  }

}
