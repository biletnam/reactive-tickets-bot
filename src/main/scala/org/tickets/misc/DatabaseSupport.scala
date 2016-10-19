package org.tickets.misc


/**
  * Created by bsnisar on 19.10.16.
  */
trait DatabaseSupport {
  import slick.driver.H2Driver.api._

  val db  = Database.forConfig("h2db")
}
