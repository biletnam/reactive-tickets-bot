package org.tickets.api

import akka.actor.{Actor, Props}
import org.tickets.misc.{Log, Named}


/**
  * Telegram API.
  *
  * @author bsnisar
  */
class Telegram extends Actor with Log {

  override def receive: Receive = {
    case msg @ _ => log.info("Get {}", msg)
  }

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    log.info("preStart()")
  }
}

object Telegram extends Named {
  override val name: String = "telegram"

  def props: Props = Props(classOf[Telegram])
}
