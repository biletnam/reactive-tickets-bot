package com.github.bsnisar.tickets.misc

import org.json4s.jackson.Serialization

trait Json {

  /**
    * Main configuration for json4s
    */
  implicit val formats = org.json4s.DefaultFormats

  /**
    * Jackson support for serialization.
    */
  implicit val serialization = org.json4s.jackson.Serialization
}
