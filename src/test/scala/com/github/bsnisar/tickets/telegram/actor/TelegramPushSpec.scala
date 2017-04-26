package com.github.bsnisar.tickets.telegram.actor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.testkit.TestActorRef
import com.github.bsnisar.tickets.AkkaBaseTest
import com.github.bsnisar.tickets.misc.TemplatesFreemarker
import com.github.bsnisar.tickets.telegram.{MsgSimple, TelegramUpdates}
import com.github.bsnisar.tickets.wire.{MockWire, SpyWire}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class TelegramPushSpec extends AkkaBaseTest(ActorSystem()) {

  "A TelegramPush" should "send message" in {
    implicit val mt = ActorMaterializer()
    implicit val tpl = new TemplatesFreemarker
    val json = "{\"message_id\": 12}"
    val wire = new SpyWire(new MockWire(json))

    val ref = TestActorRef(TelegramPush.props(wire, tpl))
    ref ! TelegramUpdates.Reply("41", MsgSimple('test, Map("name" -> "test_name")))

    wire.awaitTransmission(3.seconds)
    val (req, _) = wire.requests.get(0)
    val content = req.entity.dataBytes
      .runWith(Sink.head)
      .map(_.utf8String)

    val body = Await.result(content, Duration.Inf)
    assert(body === "{\"chat_id\":\"41\",\"text\":\"Text test_name\"}")

  }

}
