package org.tikets.uz

import java.time.LocalDate

trait Routes {
  def withDeparture(id: String): Routes
  def withArrive(id: String): Routes
  def withArriveAt(data: LocalDate): Routes
  def fetch: Iterable[Route]
}
