package org.tickets.bot

import akka.actor.{Actor, ActorSystem}
import org.tickets.bot.Bot.TimeMark
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Update

import scala.concurrent.duration.FiniteDuration

object Bot {

  /**
    * Input message for Actor.
    * @param text test for input
    * @param update whole update [[Update]]
    */
  case class Cmd(text: String, update: Update)

  /**
    * Some action during time
    * @param seq generation seq number
    */
  case class TimeMark(seq: Int) {
    def expired(bot: Bot): Boolean = seq == bot.ttlSeqNum
  }

}

trait Bot extends Actor with LogSlf4j {
  type Cmd = Bot.Cmd

  private var ttlSeqNum: Int = 0

  protected final def becomeOf(receive: Receive, ttl: FiniteDuration = null): Unit = {
    if (ttl != null) {
      val system: ActorSystem = context.system
      implicit val ec = system.dispatcher

      system.scheduler.scheduleOnce(
        delay = ttl, receiver = self, message = TimeMark(ttlSeqNum))

      ttlSeqNum += 1
    }
    context become receive
  }
}