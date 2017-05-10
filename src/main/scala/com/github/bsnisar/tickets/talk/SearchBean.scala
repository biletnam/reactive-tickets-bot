package com.github.bsnisar.tickets.talk

import java.time.LocalDateTime



trait SearchBean {
  /**
    * Time to arrive.
    * @return arrivals
    */
  def arrive: List[LocalDateTime]

  /**
    * Times to departure.
    * @return departures
    */
  def departure: List[LocalDateTime]

  /**
    * Id of the arrival station.
    * @return station id
    */
  def arriveTo: Option[String]

  /**
    * Departure station id.
    * @return id
    */
  def departureFrom: Option[String]


  def define: Boolean = arrive.nonEmpty && departure.nonEmpty && arriveTo.isDefined && departureFrom.isDefined

  def withArrive(time: LocalDateTime): Bean
  def withArriveTo(time: String): Bean
  def withDeparture(time: LocalDateTime): Bean
  def withDepartureFrom(time: String): Bean
}

sealed trait Bean
case class Modified(bean: SearchBean) extends Bean
case class Req(bean: SearchBean) extends Bean

case class Default(arrive: List[LocalDateTime] = Nil,
                   departure: List[LocalDateTime] = Nil,
                   arriveTo: Option[String] = None,
                   departureFrom: Option[String] = None) extends SearchBean {

  override def withArrive(time: LocalDateTime): Bean = {
    toResultingBean(copy(arrive = List(time)))
  }

  override def withArriveTo(id: String): Bean =
    toResultingBean(copy(arriveTo = Option(id)))

  override def withDeparture(time: LocalDateTime): Bean = {
    toResultingBean(copy(departure = List(time)))
  }

  override def withDepartureFrom(id: String): Bean =
    toResultingBean(copy(departureFrom = Option(id)))


  private def toResultingBean(res: SearchBean): Bean = {
    if (res.define) Req(res) else Modified(res)
  }
}
