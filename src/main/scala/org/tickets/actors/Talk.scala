package org.tickets.actors

import akka.actor.{Actor, ActorRef, Props}
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Message

object Talk {
  def props(delegate: Props): Props
    = Props(classOf[Talk], delegate)
}

/**
  * Client conversation. Responsible for error handling, and updates deduplication
  * @param props define station props
  */
class Talk(val props: Props) extends Actor with LogSlf4j {
  private var lastUpdateSeqNum: Int = Int.MinValue
  private val talk: ActorRef = context.actorOf(props, "talk")

  override def receive: Receive = {
    case update: Message if update.id > lastUpdateSeqNum =>
      lastUpdateSeqNum = update.id
      talk ! Bot.Cmd(update.text, update)
  }
}