package com.github.bsnisar.tickets.actors

import akka.actor.FSM
import com.github.bsnisar.tickets.{Update, UpdateText}
import com.github.bsnisar.tickets.actors.TalkActor.{Idle, Msg, Talk}

object TalkActor {

  trait Msg
  case object Idle extends Msg
  case object Qtn extends Msg


  type Talk = Map[Symbol, Any]

}

class TalkActor extends FSM[Msg, Talk] {

  when(Idle) {
    case Event(UpdateText("/from"), _)  => stay()
    case Event(UpdateText("/to"), _)  => stay()
    case Event(UpdateText("/arriveAt"), _)  => stay()
  }

}
