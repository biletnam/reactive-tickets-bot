package org.tickets.railway.spy


/**
  * Constant station object.
  * @param uid identifier
  * @param apiCode api id
  * @param name name
  * @param provider provider
  */
case class StationConst(uid: String,
                        apiCode: String,
                        name: String,
                        provider: String) extends Station

/**
  * Mock helper.
  */
object StationMock {
  def apply(id: String, name: String): Station =
    StationConst(id, id, name, "uz")
}
