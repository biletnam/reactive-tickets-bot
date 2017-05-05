package com.github.bsnisar.tickets.telegram

import java.util.Locale
import java.{util => jUtil}

import com.github.bsnisar.tickets.Station
import com.github.bsnisar.tickets.talk.TalkEntity
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

case object MsgStationsNotFound extends Msg {
  override def id: Symbol = Msg.StationsNotFound
  override def params: jUtil.Map[String, Any] = ImmutableMap.of()
}

final case class MsgCommandFailed(id: Symbol = Msg.Failure, cmd: String) extends Msg {
  override def params: jUtil.Map[String, Any] = ???
}

final case class MsgQueryUpdate(id: Symbol = Msg.QueryUpdated, talkEntity: TalkEntity) extends Msg {
  override def params: jUtil.Map[String, Any] = ???
}

final case class MsgQueryExecute(id: Symbol = Msg.QueryExecuted, talkEntity: TalkEntity) extends Msg {
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