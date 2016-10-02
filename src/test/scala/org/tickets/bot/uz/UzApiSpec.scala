package org.tickets.bot.uz

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import com.google.common.base.Suppliers
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import org.tickets.misc.EmptyContext

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class UzApiSpec extends FunSuite with Matchers with BeforeAndAfterAll {

  implicit val as = ActorSystem("test")
  implicit val mt = ActorMaterializer()

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(as)
  }

  test("flow prepopulate requests with API token") {
    val flow = UzApi.withToken(Suppliers.ofInstance("token"))
    val resp = Source.single(HttpRequest() -> EmptyContext)
      .via(flow)
      .runWith(Sink.head)

    val req = Await.result(resp, Duration.Inf)._1

    assert(req.headers.exists(_.name() == "GV-Token"))
  }


}
