package com.github.bsnisar.tickets.sub

import com.jcabi.xml.XML

trait Subscriptions {

  /**
    * Subscribe client to particular query.
    *
    * @param client client id
    * @param query trains request
    */
  def add(client: String, query: XML): Unit

}
