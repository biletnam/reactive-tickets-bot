package com.github.bsnisar.tickets.telegram

import java.time.format.DateTimeFormatter
import java.util.Locale
import java.{util => jUtil}

import com.github.bsnisar.tickets.Station
import com.github.bsnisar.tickets.talk.SearchBean
import com.google.common.collect.ImmutableMap

/**
  * Template message payload.
  */
sealed trait Msg {

  /**
    * Template id
    * @return id
    */
  def id: Symbol

  /**
    * Template locale.
    * @return local.
    */
  def local: Locale = Locale.ENGLISH

  /**
    * Template params.
    * @return params.
    */
  def params: jUtil.Map[String, Any]
}

object Msg {
  val WrongTimeFormat = 'wrong_time_format
  val Hello = 'hello_world
  val Failure = 'cmd_failed
  val QueryUpdated = 'query_updated
  val QueryExecuted = 'query_executed
  val StationsFoundFrom = 'stations_found_from
  val StationsFoundTo = 'stations_found_to
  val StationsNotFound = 'stations_not_found
}

final case class MsgStationsFound(id: Symbol, stations: Iterable[Station], byName: String) extends Msg {
  override def params: jUtil.Map[String, Any] = {
    import scala.collection.JavaConverters._
    val params = stations.map(station => ImmutableMap.of(
      "id", station.id,
      "name", station.name)
    ).asJava

    ImmutableMap.of("stations", params, "keyword", byName)
  }
}

final case class MsgWrongTimeFormat(timeStr: String) extends Msg {
  override def id: Symbol = Msg.WrongTimeFormat
  override def params: jUtil.Map[String, Any] = ImmutableMap.of("time", timeStr)
}

case object MsgStationsNotFound extends Msg {
  override def id: Symbol = Msg.StationsNotFound
  override def params: jUtil.Map[String, Any] = ImmutableMap.of()
}

final case class MsgCommandFailed(id: Symbol = Msg.Failure, cmd: String) extends Msg {
  override def params: jUtil.Map[String, Any] = ???
}

final case class MsgQueryUpdate(bean: SearchBean) extends Msg {
  override val id: Symbol = Msg.QueryUpdated
  override def params: jUtil.Map[String, Any] = {
    val dep = if (bean.departure.nonEmpty) {
      bean.departure.head.format(DateTimeFormatter.ISO_DATE_TIME)
    } else {
      "please specify by '/departureAt YYYY-MM-DD'"
    }

    val arr = if (bean.arrive.nonEmpty) {
      bean.departure.head.format(DateTimeFormatter.ISO_DATE_TIME)
    } else {
      "please specify by '/arriveAt YYYY-MM-DD'"
    }

    ImmutableMap.of(
      "departureAt", dep,
      "arriveAt", arr,
      "departure", "not define",
      "arrive", "not define"
    )
  }
}

final case class MsgQueryExecute(id: Symbol = Msg.QueryExecuted, talkEntity: SearchBean) extends Msg {
  override def params: jUtil.Map[String, Any] = ???
}

case object MsgHello extends Msg {
  override def id: Symbol = Msg.Hello
  override def params: jUtil.Map[String, Any] = ImmutableMap.of()
}

/**
  * Some simple message.
  */
final case class MsgSimple(id: Symbol, context: Map[String, Any] = Map.empty) extends Msg {
  override def params: jUtil.Map[String, Any] = {
    import scala.collection.JavaConverters._
    context.asJava
  }
}