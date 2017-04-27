package com.github.bsnisar.tickets.telegram.actor

import akka.actor.{Actor, ActorRef, Props, Status}
import akka.stream.Materializer
import com.github.bsnisar.tickets.telegram.Telegram
import com.github.bsnisar.tickets.telegram.TelegramUpdates.{Update, Updates}
import com.github.bsnisar.tickets.telegram.actor.PullActor.Tick
import com.typesafe.scalalogging.LazyLogging

object PullActor {

  def props(tgUpdates: Telegram, hub: ActorRef, materializer: Materializer): Props =
    Props(classOf[PullActor], tgUpdates, hub, materializer)

  case object Tick
}
class PullActor(val tgUpdates: Telegram,
                val hub: ActorRef)(implicit val m: Materializer) extends Actor with LazyLogging {
  import akka.pattern.pipe
  import context.dispatcher

  private[this] var offset = 1

  override def receive: Receive = {
    case Tick =>
      tgUpdates.pull(offset)
        .pipeTo(self)

    case Seq(messages) =>
      hub ! messages
    case Status.Failure(error) =>
      logger.error("pull call failed", error)
  }
}
