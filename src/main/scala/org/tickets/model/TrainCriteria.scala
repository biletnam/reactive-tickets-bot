package org.tickets.model

import java.time.LocalDate
import java.util.Comparator

import org.json4s
import org.json4s.JValue
import org.tickets.misc.LocalDateComparator

import scala.math.Ordered

/**
  * Criteria for search tickets.
  * @param fromStation from station
  * @param toStation to station
  * @param arrivals arrivals
  */
case class TrainCriteria(fromStation: Station,
                         toStation: Station,
                         arrivals: List[LocalDate])



object TrainCriteria {

  implicit val LocalDateOrdering = new Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate): Int =
      LocalDateComparator.COMPARATOR.compare(x, y)
  }

  def newCriteria(fromStation: Station,
                   toStation: Station,
                   arrivals: List[LocalDate]): TrainCriteria = {

    new TrainCriteria(
      fromStation, toStation, arrivals.sorted
    )
  }

}
