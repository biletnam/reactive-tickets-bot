package com.github.bsnisar.tickets.actors

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.Update

final class Chats(private val prop: Props) extends Actor {
  private var chats = Map.empty[Long, ActorRef]

  override def receive: Receive = {
    case update: Update =>
      chats.get(update.chat) match {
        case Some(ref) =>
          ref ! update

        case None =>
          val ref = context.actorOf(prop)
          chats = chats + (update.chat -> ref)
          ref ! update
      }
  }
}
