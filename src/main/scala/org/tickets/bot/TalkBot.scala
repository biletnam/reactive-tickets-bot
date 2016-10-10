package org.tickets.bot

import akka.actor.{Actor, Props}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j

object TalkBot {
  def props(chatId: String): Props = Props(classOf[TalkBot])
}

class TalkBot(chatId: String) extends Actor with LogSlf4j {
  import org.json4s.DefaultReaders._

  override def receive: Receive = readMessage()


  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    log.debug("register talk to char = {}", chatId)
  }

  def readMessage(): Receive = {
    case msg: JValue  =>
      val text = (msg \ "text").as[String]
      onCommand(msg, text)
    case e @ _ =>
      log.warn("Unhandled message {}", e)
  }

  def onCommand(msg: JValue, text: String): Unit = text match {
    case "/start" =>
      log.debug("Start talk...")
    case "/new_route" =>

  }
}
