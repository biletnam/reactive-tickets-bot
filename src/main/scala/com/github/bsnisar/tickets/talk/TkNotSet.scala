package com.github.bsnisar.tickets.talk

/**
  * Empty talk graph.
  * @author bsnisar
  */
object TkNotSet extends Talk{
  override def act(cmd: String): Either[Talk, Any] = throw new IllegalStateException("empty talk")
}
