package org.tickets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j

object TalkBot {
  def props(chatId: String): Props = Props(classOf[TalkBot])
}

class TalkBot(chatId: String) extends Actor with LogSlf4j {
  import org.json4s.DefaultReaders._

  override def receive: Receive = actionCommands()

  private def actionCommands(): Receive = {
    case update: JValue =>
      extractTextMessage(update) match {
        case "/start" =>
          println("CLear it")
      }

  }

  private def routesQueryCmd(query: ActorRef): Receive = {
    case update: JValue =>

      extractTextMessage(update) match {
        case "clear" =>
          println("CLear it")
        case cmd: String =>
          query ! cmd
      }
  }

  private def extractTextMessage(update: JValue): String = {
    val msg = update \ "message"
    val text = (msg \ "text").as[String]
    text
  }

}
