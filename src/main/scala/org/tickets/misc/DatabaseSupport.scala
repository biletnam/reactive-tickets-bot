package org.tickets.misc

import slick.driver.H2Driver
import slick.driver.H2Driver.api._


/**
  * Created by bsnisar on 19.10.16.
  */
object DatabaseSupport {
  type DB = H2Driver.backend.Database

}
