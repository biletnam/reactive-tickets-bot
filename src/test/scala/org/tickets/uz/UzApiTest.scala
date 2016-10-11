package org.tickets.uz

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}
import org.tickets.uz.cmd.FindStationsCommand

/**
  * Created by Bogdan_Snisar on 10/4/2016.
  */
class UzApiTest extends TestKit(ActorSystem("test")) with FunSuiteLike with BeforeAndAfterAll with Matchers {

  override def afterAll {
    shutdown()
  }

  test("use infrastructure") {
    val sender = TestProbe()
    implicit val mt = ActorMaterializer()

    val publisherRef: ActorRef = UzApi.publisherRef
    publisherRef ! FindStationsCommand.request("Днепр", sender.ref)
    sender expectMsg "found"
  }

}
