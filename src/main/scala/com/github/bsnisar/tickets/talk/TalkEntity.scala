package com.github.bsnisar.tickets.talk

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait TalkEntity {
  def arrive: Option[LocalDateTime]
  def departure: Option[LocalDateTime]
  def arriveTo: Option[String]
  def departureFrom: Option[String]

  def complete: Boolean = arrive.isDefined && departure.isDefined &&
    arriveTo.isDefined && departureFrom.isDefined


  def withArrive(time: String): TalkEntity
  def withArriveTo(time: String): TalkEntity
  def withDeparture(time: String): TalkEntity
  def withDepartureFrom(time: String): TalkEntity
}

case class Default(arrive: Option[LocalDateTime] = None,
                   departure: Option[LocalDateTime] = None,
                   arriveTo: Option[String] = None,
                   departureFrom: Option[String] = None) extends TalkEntity {
  override def withArrive(time: String): TalkEntity = {
    val t = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
    copy(arrive = Some(t))
  }

  override def withArriveTo(id: String): TalkEntity =
    copy(arriveTo = Option(id))

  override def withDeparture(time: String): TalkEntity = {
    val t = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
    copy(departure = Some(t))
  }

  override def withDepartureFrom(id: String): TalkEntity =
    copy(departureFrom = Option(id))
}
