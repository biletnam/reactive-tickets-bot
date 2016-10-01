package org.tickets.misc

import akka.actor.ActorRef
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.util.Try

/**
  * Created by bsnisar on 29.09.16.
  */
object HttpSupport {

  type Request = (HttpRequest, Bound)
  type Response = (Try[HttpResponse], Bound)

  type Bound = Req

  /**
    * Request context
    */
  trait Req {
  }

  /**
    * Empty context.
    */
  case object EmptyContext extends Req

  case class WithSender(ref: ActorRef) extends Req

  /**
    * Command from actor.
    */
  case class Command[T](senderRef: ActorRef, payload: T, seq: Int = 1) extends Req {

    def withNext: Command[T] = this.copy(seq = seq + 1)
  }
}
