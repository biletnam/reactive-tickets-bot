package org.tickets.railway.spy

trait Station {

  /**
    * System unique id.
    * @return id
    */
  def uid: String

  /**
    * API station id.
    * @return id specific for API
    */
  def apiCode: String

  /**
    * Name of station. Can be in a different locale.
    * @return name
    */
  def name: String
}