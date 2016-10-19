package org.tickets.railway.spy



case class StationConst(uid: String, apiCode: String, name: String, provider: String) extends Station

object StationMock {
  def apply(id: String, name: String): Station =
    StationConst(id, id, name, "uz")
}
