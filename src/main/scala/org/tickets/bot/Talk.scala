package org.tickets.bot

import org.json4s.DefaultReaders._
import akka.actor.{Actor, ActorRef, ActorSelection, PoisonPill, Props}
import org.json4s.JValue
import org.tickets.misc.LogSlf4j

object Talk {
  def props(chatId: String): Props = Props(classOf[Talk])
}



class Talk(chatId: String, telegram: ActorRef, railway: ActorRef) extends Actor with LogSlf4j  {
  private val route: ActorRef = context.actorOf(QuerySpec.props(self, railway), "/route")
  private val search: ActorSelection = context.actorSelection("/route/search")

  override def receive: Receive = {

    // telegram update
    case update: JValue =>
      val text = (update \ "message" \ "text").as[String]
      log.debug("on command: {}", text)
      onCommand(text)

    // query for search
    case e @ QueryProtocol.DefQuery(_,_,_) =>
      log.debug("query defined")
      search ! e

    // msg to telegram
    case msg: UserInteractions.TelegramMessage =>
      log.debug("to telegram")
  }

  def onCommand(cmd: String): Unit = cmd match {
    case "/start" =>

    case "/new_route" =>
      route ! QueryProtocol.Start

    case "/clear" =>
      route ! QueryProtocol.Reload

    case any: String =>
      route ! any
  }


}
