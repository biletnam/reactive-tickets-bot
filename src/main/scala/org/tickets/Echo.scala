package org.tickets

import akka.actor.Actor
import akka.actor.Actor.Receive
import org.tickets.telegram.Push.TextMsg
import org.tickets.telegram.Update

/**
  * Created by bsnisar on 12.10.16.
  */
class Echo extends Actor {
  override def receive: Receive = {
    case up: Update =>
      println("$$ Text:    " + up.text)
      sender() ! TextMsg(up.chat, "Hello World")
  }
}
