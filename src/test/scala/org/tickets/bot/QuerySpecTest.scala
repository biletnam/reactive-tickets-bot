package org.tickets.bot

import akka.actor.ActorSystem
import akka.testkit.{TestFSMRef, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuiteLike, Matchers}
import org.tickets.bot.QuerySpec._
import org.tickets.misc.{ToStringId, UniqueIndex}
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand

trait MockUniqueIndex extends UniqueIndex {
  override def groupBy[T: ToStringId](entries: Seq[T]): Map[String, T] = {
    entries.zip(1 to entries.size).foldLeft(Map.empty[String, T])((map, idxEntry) => map + (idxEntry._2.toString -> idxEntry._1))
  }
}

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
    spec = TestFSMRef(new QuerySpec(uzProb.ref, parent.ref) with MockUniqueIndex)
  }

  test("idle on start") {
    spec.stateName shouldBe Idle
    spec.stateData shouldBe EmptyParam
  }

  test("wait for input on start up") {
    spec ! QueryProtocol.Start
    parent expectMsg Message.NeedDepartureStation
    spec.stateName shouldBe WaitInput
    spec.stateData shouldBe EmptyParam
  }

  test("ask api for stations after name input") {
    spec.setState(WaitInput)
    spec ! "Dn"
    uzProb expectMsgAllClassOf classOf[FindStationsCommand]
    spec.stateName shouldBe WaitApiSearch
    spec.stateData shouldBe PartialData(Todo.Dest, List(Todo.Src, Todo.ArriveAt), Map())
  }

  test("on api success search response send msg to client for selection") {
    spec.setState(stateName = WaitApiSearch, stateData = PartialData())
    val station: Station = Station("10001", "Dn-01")
    val variants: Map[String, Station] = Map("1" -> station)

    spec ! FindStationsCommand.StationHits(List(station))
    parent expectMsg Message.PickUpStation(variants)
    spec.stateName shouldBe WaitClientChoice
    spec.stateData shouldBe TmpChoices(variants, PartialData())
  }

  test("wait client choice and go for remain station search") {
    val station: Station = Station("10001", "Dn-01")
    val variants: Map[String, Station] = Map("1" -> station)
    spec.setState(stateName = WaitClientChoice, stateData = TmpChoices(variants, PartialData()))

    spec ! "1"
    parent.expectMsgPF() {
      case Message.NeedDepartureStation => Unit
      case Message.NeedArrivalState => Unit
    }
    spec.stateName shouldBe WaitInput
    spec.stateData shouldBe PartialData(Todo.Src, List(Todo.ArriveAt), Map(Todo.Dest -> station))
  }

  test("on ready query push it to parent") {
    val stationFrom = Station("10001", "Dn-01")
    val stationTo = Station("10002", "Zp-01")
    spec.setState(stateName = WaitClientChoice, stateData = PartialData())
  }
}
