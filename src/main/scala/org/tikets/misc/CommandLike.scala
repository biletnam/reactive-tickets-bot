package org.tikets.misc

import akka.persistence.PersistentActor

/**
  * Created by bsnisar on 14.09.16.
  */
trait CommandLike extends PersistentActor {

  private var act: Receive = default

  final override def receiveCommand: Receive = {
    case msg => act(msg)
  }

  def default: Receive

  def become(act: Receive) = {
    this.act = act
  }

}
