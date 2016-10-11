package org.tickets

import java.util.{Locale, ResourceBundle}

import org.tickets.uz.Station

object UserInteractions {

  private lazy val _bundle = ResourceBundle.getBundle("Messages", Locale.ENGLISH)

  sealed trait TextMessage {
    def test: String = ""
  }

  /**
    * Ask client for departure station.
    */
  case object NeedDepartureStation extends TextMessage {
    override def test: String = _bundle.getString("ask.departure")
  }

  /**
    * Ask client for departure station.
    */
  case class PickUpStation(variants: Map[String, Station]) extends TextMessage {
    override def test: String = _bundle.getString("ask.pickup.stations")
  }

  /**
    * Ask client for arrival station.
    */
  case object NeedArrivalState extends TextMessage {
    override def test: String = _bundle.getString("ask.arrival")
  }
}
