package org.tickets.bot

import java.util.{Locale, ResourceBundle}

import org.tickets.uz.Station

object UserInteractions {

  private lazy val _bundle = ResourceBundle.getBundle("Messages", Locale.ENGLISH)

  /**
    * Simple text telegram message
    */
  trait SimpleText { this: TelegramMessage =>

    override final def toMap(chat: String): Map[String, Any] =
      Map("chat_id" -> chat, "text" -> text)

    /**
      * Raw text for message
      * @return
      */
    def text: String
  }

  sealed trait TelegramMessage {
    def toMap(chat: String): Map[String, Any] = Map.empty
  }

  /**
    * Ask client for departure station.
    */
  case object NeedDepartureStation extends TelegramMessage with SimpleText {
    override def text: String = _bundle.getString("ask.station.departure")
  }

  /**
    * Ask client for arrival station.
    */
  case object NeedArrivalState extends TelegramMessage with SimpleText {
    override def text: String = _bundle.getString("ask.station.arrival")
  }

  case object Hello extends TelegramMessage with SimpleText {
    override def text: String = _bundle.getString("hello.world")
  }

  /**
    * Ask client for departure station.
    */
  case class PickUpStation(variants: Map[String, Station]) extends TelegramMessage {
  }
}
