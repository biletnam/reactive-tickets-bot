package org.tikets.bot

import akka.actor.{Actor, ActorRef, Props}
import org.tikets.misc.Log
import org.tikets.msg.{ID, Msg, Phrase}

object Talks {

  case class Cmd(keyword: String)


}

/**
  * Talks.
  * Each user has talk by its chat room.
  *
  * @author bsnsiar
  */
class Talks extends Actor with Log {

  /**
    * Talks registry.
    */
  private var talks: Map[ID, ActorRef] = Map.empty

  override def receive: Receive = {
    case msg: Msg => onUpdate(msg)
  }

  private def onUpdate(msg: Msg) = {
    val id: ID = msg.id
    val ref = obtainRef(id)
    ref ! msg
  }

  private def obtainRef(id: ID): ActorRef = {
    val maybeRef = talks.get(id)

    if (maybeRef.isDefined) {
      maybeRef.get
    } else {
      val name = s"tk-${id.user}-${id.chat}"
      val ref = context.actorOf(Props[RoutesTalk], name)
      log.debug("Talks#obtainRef(): new {}", name)
      talks = talks + (id -> ref)
      ref
    }
  }

}
