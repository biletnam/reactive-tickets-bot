package org.tickets.bot

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.tickets.bot.Telegram.TelegramMethod

import scala.util.{Success, Try}
import scala.concurrent.duration._

/**
  * Created by bsnisar on 25.09.16.
  */
class TelegramSpec extends TestKit(ActorSystem("test")) with FlatSpecLike with BeforeAndAfterAll with Matchers  {

  val json =
    """
      |[
      | {
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
      | }
      |]
    """.stripMargin

  override def afterAll {
    shutdown()
  }

  "A Telegram "  should "parse json response and push notifications" in {
    val mt = ActorMaterializer()
    val flow: Flow[(HttpRequest, TelegramMethod), (Try[HttpResponse], TelegramMethod), _] =
      Flow.fromFunction(req =>
        Success(HttpResponse(
          entity = HttpEntity(contentType = ContentTypes.`application/json`, json)
        )) -> req._2
      )

    val ref = TestActorRef[Telegram](Props(classOf[Telegram], flow, mt))
    expectMsg(max = 6.seconds, obj = "Hi")
  }
}
