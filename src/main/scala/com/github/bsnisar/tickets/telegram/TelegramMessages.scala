package com.github.bsnisar.tickets.telegram

import java.util.Locale
import java.{util => jUtil}

import com.github.bsnisar.tickets.Station
import com.github.bsnisar.tickets.misc.Template
import com.google.common.collect.ImmutableMap

object TelegramMessages {

  val FromStationsFound = 'from_stations_found
  val ToStationsFound = 'to_stations_found


  /**
    * Template message payload.
    */
  sealed trait SendMsg {

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

    /**
      * Create message test.
      * @param t template processor
      * @return message
      */
    def mkPayload(implicit t: Template): String = t.eval(this)
  }

  final case class MsgFoundStations(id: Symbol, stations: Iterable[Station]) extends SendMsg {
    override def params: jUtil.Map[String, Any] = {
      import scala.collection.JavaConverters._
      val params = stations.map(station => ImmutableMap.of(
        "id", station.id,
        "name", station.name)
      ).asJava

      ImmutableMap.of("stations", params)
    }
  }


  final case class MsgCommandFailed(id: Symbol = 'cmd_failed, cmd: String) extends SendMsg {
    override def params: jUtil.Map[String, Any] = ???
  }

  /**
    * Some simple message.
    */
  final case class MsgSimple(id: Symbol, context: Map[String, Any] = Map.empty) extends SendMsg {
    override def params: jUtil.Map[String, Any] = {
      import scala.collection.JavaConverters._
      context.asJava
    }
  }

}
