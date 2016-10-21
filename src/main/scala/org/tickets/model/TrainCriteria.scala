package org.tickets.model

import java.time.LocalDate

import org.json4s
import org.json4s.JValue

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

}
