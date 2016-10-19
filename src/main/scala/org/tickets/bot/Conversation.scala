package org.tickets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Message

object Conversation {
  def props(delegate: Props): Props
    = Props(classOf[Conversation], delegate)
}

/**
  * Client conversation. Responsible for error handling, and updates deduplication
  * @param props define station props
  */
class Conversation(val props: Props) extends Actor with LogSlf4j {
  private var lastUpdateSeqNum: Int = Int.MinValue
  private val talk: ActorRef = context.actorOf(props, "talk")

  override def receive: Receive = {
    case update: Message if update.id > lastUpdateSeqNum =>
      lastUpdateSeqNum = update.id
      talk ! Bot.Cmd(update.text, update)
  }
}