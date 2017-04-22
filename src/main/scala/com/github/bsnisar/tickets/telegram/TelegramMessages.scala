package com.github.bsnisar.tickets.telegram

import java.util.Locale
import java.{util => jUtil}

import com.github.bsnisar.tickets.Station

object TelegramMessages {

  /**
    * Template based message.
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
  }

  final case class MsgFoundStations(id: Symbol, stations: Iterable[Station]) extends SendMsg {
    override def params: jUtil.Map[String, Any] = ???
  }


  final case class MsgCommandFailed(id: Symbol = 'cmd_failed, cmd: String) extends SendMsg {
    override def params: jUtil.Map[String, Any] = ???
  }

  /**
    * Some simple message.
    */
  final case class MsgSimple(id: Symbol, context: Map[String, Any] = Map.empty) extends SendMsg {
    override def params: jUtil.Map[String, Any] = ???
  }

}
