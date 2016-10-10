package org.tickets

object UserInteractions {
  trait TextMessage {
    def test: String = ""
  }

  case object NeedDepartureStation extends TextMessage
}
