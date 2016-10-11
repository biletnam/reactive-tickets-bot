package org.tickets.misc

import java.util.concurrent.ThreadLocalRandom

import org.tickets.uz.Station

object UniqueIndex {
  implicit val randomStringId: ToStringId[Station] = new RandomStringId
}

/**
  * Group entries by unique identifier. Delegate it to ToStringId implicit generator.
  * @author Bogdan_Snisar
  */
trait UniqueIndex {

  def groupBy[T: ToStringId](entries: Seq[T]): Map[String, T] = {
    val toStringId = implicitly[ToStringId[T]]
    entries.zip(1 to entries.size).foldLeft(Map.empty[String, T])((map, idxEntry) => map + (toStringId(idxEntry._1, idxEntry._2) -> idxEntry._1))
  }
}

/**
  * Produce string identification for entry.
  * @tparam T entry type
  * @author Bogdan_Snisar
  */
trait ToStringId[T] {
  def apply(entry: T): String = apply(entry, 1)
  def apply(entry: T, idx: Int): String
}

class RandomStringId extends ToStringId[Station] {
  override def apply(entry: Station, idx: Int): String = {
    s"st_${Integer.toHexString(ThreadLocalRandom.current().nextInt(5000) + idx)}"
  }
}