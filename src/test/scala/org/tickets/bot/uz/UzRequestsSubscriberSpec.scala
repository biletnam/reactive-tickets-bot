package org.tickets.bot.uz

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.stream.ActorMaterializer
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import org.tickets.bot.uz.FindStationsCommand.StationHits
import org.tickets.misc.Req

import scala.util.Success

class UzRequestsSubscriberSpec extends TestKit(ActorSystem("test")) with FlatSpecLike
  with BeforeAndAfterAll with Matchers {

  override def afterAll {
    shutdown()
  }

  "A UzRequestsSubscriber" should " consume http response for station search" in {
    val actor = system.actorOf(UzApiSubscriber.props(ActorMaterializer()))
    val sender = TestProbe()
    val json = """
                 | {
                 |   "value":[
                 |      {
                 |         "title":"\u0414\u043d\u0435\u043f\u0440\u043e\u0432\u0441\u043a\u0430\u044f",
                 |         "station_id":"2208327"
                 |      },
                 |      {
                 |         "title":"\u0414\u043d\u0435\u043f\u0440\u043e\u0434\u0437\u0435\u0440\u0436\u0438\u043d\u0441\u043a",
                 |         "station_id":"2210650"
                 |      }
                 |   ],
                 |   "error":null,
                 |   "data":{
                 |      "req_text":[
                 |         "\u0434\u043d",
                 |         "ly"
                 |      ]
                 |   },
                 |   "captcha":null
                 |}
               """.stripMargin
    val httpResponse = HttpResponse(entity = HttpEntity(json).withContentType(ContentTypes.`application/json`))

    val envelope: Req = FindStationsCommand(sender.ref)
    actor ! (Success(httpResponse), envelope)
    sender expectMsg StationHits(
      Station("2210650", "Днепродзержинск") :: Station("2208327", "Днепровская") :: Nil
    )
  }

}
