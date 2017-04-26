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

final case class MsgFoundStations(id: Symbol, stations: Iterable[Station]) extends Msg {
  override def params: jUtil.Map[String, Any] = {
    import scala.collection.JavaConverters._
    val params = stations.map(station => ImmutableMap.of(
      "id", station.id,
      "name", station.name)
    ).asJava

    ImmutableMap.of("stations", params)
  }
}


final case class MsgCommandFailed(id: Symbol = 'cmd_failed, cmd: String) extends Msg {
  override def params: jUtil.Map[String, Any] = ???
}

final case class MsgQueryUpdate(id: Symbol = 'query_updated, talkEntity: TalkEntity) extends Msg {
  override def params: jUtil.Map[String, Any] = ???
}

final case class MsgQueryExecute(id: Symbol = 'query_executed, talkEntity: TalkEntity) extends Msg {
  override def params: jUtil.Map[String, Any] = ???
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