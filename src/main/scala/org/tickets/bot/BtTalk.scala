package org.tickets.bot

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j
import org.json4s.DefaultReaders._

object BtTalk {
  def props(chatId: String): Props = Props(classOf[BtTalk])
}

/**
  * Handle exact text message from client
  */
trait CommandMessage extends Actor {

  override def receive: Receive = {
    case update: JValue =>
      val text = (update \ "message" \ "text").as[String]
      onMessage(text)
  }

  def onMessage: PartialFunction[String, Any]
}

class BtTalk(chatId: String) extends Actor with LogSlf4j with CommandMessage {

  private var routeTalk: ActorRef = _

  override def onMessage: PartialFunction[String, Any] = {
    case "/start" => ???
    case "/route" =>
      routeTalk = context.actorOf(BtRouteQuery.props(self))
    case "/clear" =>
      routeTalk ! PoisonPill
      routeTalk = context.actorOf(BtRouteQuery.props(self))
  }
}

