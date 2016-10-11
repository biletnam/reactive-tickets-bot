package org.tickets.bot

import akka.actor.ActorSystem
import akka.testkit.{TestFSMRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuiteLike, Matchers}
import org.tickets.UserInteractions
import org.tickets.bot.QuerySpec.{EmptyParam, Query, WaitApiSearch, WaitInput}
import org.tickets.uz.cmd.FindStationsCommand

class QuerySpecTest extends TestKit(ActorSystem("test")) with FunSuiteLike
  with BeforeAndAfterAll with Matchers with BeforeAndAfterEach {
  override def afterAll {
    shutdown()
  }

  var uzProb: TestProbe = _
  var parent: TestProbe = _
  var spec: TestFSMRef[QuerySpec.QueryStatus, QuerySpec.Param, QuerySpec] = _

  override def beforeEach() {
    uzProb = TestProbe()
    parent = TestProbe()
    spec = TestFSMRef(new QuerySpec(uzProb.ref, parent.ref))
  }

  test("wait for input on start up") {
    parent expectMsg UserInteractions.NeedDepartureStation
    spec.stateName shouldBe WaitInput
    spec.stateData shouldBe EmptyParam
  }

  test("ask api for stations after name input") {
    spec ! "Dn"
    uzProb expectMsgAllClassOf classOf[FindStationsCommand]
    spec.stateName shouldBe WaitApiSearch
    spec.stateData shouldBe Query(Todo.Dest, List(Todo.Src, Todo.ArriveAt), Map())
  }
}
