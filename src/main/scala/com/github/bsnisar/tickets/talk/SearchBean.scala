package com.github.bsnisar.tickets.talk

import java.time.{LocalDate, LocalDateTime}



trait SearchBean {
  /**
    * Time to arrive.
    * @return arrivals
    */
  def arrive: List[LocalDate]

  /**
    * Times to departure.
    * @return departures
    */
  def departure: List[LocalDate]

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

  def withArrive(time: LocalDate): SearchBeanUpdate
  def withArriveTo(time: String): SearchBeanUpdate
  def withDeparture(time: LocalDate): SearchBeanUpdate
  def withDepartureFrom(time: String): SearchBeanUpdate
}

sealed trait SearchBeanUpdate
case class Modified(bean: SearchBean) extends SearchBeanUpdate
case class Req(bean: SearchBean) extends SearchBeanUpdate

case class Default(arrive: List[LocalDate] = Nil,
                   departure: List[LocalDate] = Nil,
                   arriveTo: Option[String] = None,
                   departureFrom: Option[String] = None) extends SearchBean {

  override def withArrive(time: LocalDate): SearchBeanUpdate = {
    toResultingBean(copy(arrive = List(time)))
  }

  override def withArriveTo(id: String): SearchBeanUpdate =
    toResultingBean(copy(arriveTo = Option(id)))

  override def withDeparture(time: LocalDate): SearchBeanUpdate = {
    toResultingBean(copy(departure = List(time)))
  }

  override def withDepartureFrom(id: String): SearchBeanUpdate =
    toResultingBean(copy(departureFrom = Option(id)))


  private def toResultingBean(res: SearchBean): SearchBeanUpdate = {
    if (res.define) Req(res) else Modified(res)
  }
}
