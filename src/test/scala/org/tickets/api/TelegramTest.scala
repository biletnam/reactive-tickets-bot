package org.tickets.api

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.HttpEntity.Strict
import akka.http.scaladsl.model._
import scala.concurrent.duration._
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Source}
import akka.testkit.{TestActor, TestActors, TestKit}
import akka.util.CompactByteString
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import org.tickets.msg.telegram.Update

import scala.util.{Success, Try}

class TelegramTest extends TestKit(ActorSystem("test-sys")) with FlatSpecLike with BeforeAndAfterAll {

  val msg =
    """
      |[
      |{
      | "update_id":10000,
      | "message":{
      |   "date":1441645532,
      |   "chat":{
      |      "last_name":"Test Lastname",
      |      "id":1111111,
      |      "type": "private",
      |      "first_name":"Test Firstname",
      |      "username":"Testusername"
      |   },
      |   "message_id":1365,
      |   "from":{
      |      "last_name":"Test Lastname",
      |      "id":1111111,
      |      "first_name":"Test Firstname",
      |      "username":"Testusername"
      |   },
      |   "text":"/start"
      | }
      |}
      |]
    """.stripMargin


  override protected def afterAll(): Unit = {
    shutdown()
  }


  "A Telegram" should "parse update and send message to actor" in {
    val httpStream: HttpStream = new HttpStream {
      override def source: Source[(HttpRequest, Int), _] = Source.single(HttpRequest(uri = "/hi") -> 42)
      override def flow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), _] = Flow.fromFunction(reqSq => {
        Success(HttpResponse(entity = Strict(
          contentType = ContentTypes.`application/json`, data = CompactByteString(msg))
        )) -> 42
      })
    }
    val materializer = ActorMaterializer()
    val telegramRef = system.actorOf(TestActors.echoActorProps)
    val pooler = new TelegramUpdates(httpStream, materializer, telegramRef)

    pooler.startMessagePooling()

    expectMsg[Update](max = 10.seconds, obj = Update(10, null))
  }

}
