package org.tickets.bot.uz

import org.scalatest.{FunSuite, Matchers}

class StationSpec extends FunSuite with Matchers {

  import org.json4s._
  import org.json4s.jackson._

  test("parse json correctly") {
    val json: JValue = parseJson("{\"station_id\": 21441, \"title\": \"Hello\"}")
    val station = Station.StationReader.read(json)
    station.name shouldEqual "Hello"
    station.id shouldEqual "21441"
  }
}
