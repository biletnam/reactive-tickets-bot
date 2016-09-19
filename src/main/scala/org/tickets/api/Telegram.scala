package org.tickets.api

import akka.actor.{Actor, Props}
import org.tickets.misc.{Log, Named}
import org.tickets.msg.telegram.TgUpdates


/**
  * Telegram API.
  *
  * @author bsnisar
  */
class Telegram extends Actor with Log {

  override def receive: Receive = {
    case updates: TgUpdates => log.info("Get {}", updates)
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
