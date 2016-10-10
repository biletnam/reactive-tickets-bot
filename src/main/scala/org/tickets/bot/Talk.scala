package org.tickets.bot

import org.json4s.DefaultReaders._
import akka.actor.{Actor, ActorRef, Props}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j

object Talk {
  def props(chatId: String): Props = Props(classOf[Talk])
}

/**
  * Handle text command to bot.
  */
trait TextMessage extends Actor {


  override def receive: Receive = {
    case update: JValue =>
      val text = (update \ "message" \ "text").as[String]
      onCommand(text)
  }




  /**
    * On command to bot.
    */
  def onCommand: Receive
}

class Talk(chatId: String) extends Actor with LogSlf4j with TextMessage {

  private var route: ActorRef = _

  override def onCommand: Receive = {
    case "/start" =>

    case "/route" =>
      route = context.actorOf(null, "/route")

    case any: String =>
      route ! any
  }


}
