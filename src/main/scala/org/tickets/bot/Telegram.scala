package org.tickets.bot

import java.util.{Locale, ResourceBundle}

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Source
import org.tickets.bot.Telegram.PushMsg

/**
  * Telegram API.
  */
class Telegram extends Actor {


  override def receive: Receive = {
    case "/" => ???

    case PushMsg(code, local) =>
      val bundle = ResourceBundle.getBundle("TelegramMsg", local)
      val msg = bundle.getString(code)

  }
}

object Telegram {



  case class PushMsg(msgCode: String, local: Locale = Locale.ENGLISH)

}