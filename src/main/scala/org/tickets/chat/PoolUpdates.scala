package org.tickets.chat

import akka.actor.Actor.Receive
import akka.stream.actor.ActorPublisher
import org.tickets.chat.Telegram.Req

class PoolUpdates extends ActorPublisher[Req] {

  private var lastSeq: Int = 0

  override def receive: Receive = {

  }
}
