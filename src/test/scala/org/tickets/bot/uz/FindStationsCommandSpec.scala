package org.tickets.bot.uz

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.testkit.{TestKit, TestProbe}
import com.google.inject.{Guice, Key}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import org.tickets.misc.HttpSupport.Bound
import org.tickets.module.{AkkaModule, UzModule, UzRef}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class FindStationsCommandSpec extends FunSuite with Matchers with BeforeAndAfterAll {

  private lazy val injector = Guice.createInjector(
    new AkkaModule, new UzModule
  )

  override protected def afterAll(): Unit = {
    val as = injector.getInstance(classOf[ActorSystem])
    TestKit.shutdownActorSystem(as)
  }

  test("Should uz and get response ") {
    implicit val as = injector.getInstance(classOf[ActorSystem])

    val ref: ActorRef = injector.getInstance(Key.get(
      classOf[ActorRef],
      classOf[UzRef]
    ))

    val sender = TestProbe()
    ref ! FindStationsCommand.request(sender.ref, "Днепр")
    sender expectNoMsg()
  }


  test("test flow") {
    implicit val as = injector.getInstance(classOf[ActorSystem])
    implicit val mt = injector.getInstance(classOf[Materializer])
    implicit val ec = as.dispatcher

    val sender = TestProbe()
    val flow = Http().newHostConnectionPool[Bound](UzApi.RootPageHost)
    val tf = UzApi.withTokenFlow(UzToken.singleton)

    val resp = Source.single(FindStationsCommand.request(sender.ref, "Днепр"))
      .via(tf)
      .via(flow)
      .runWith(Sink.head)

    val http = Await.result(resp, Duration.Inf)
    println(http)
  }
}
