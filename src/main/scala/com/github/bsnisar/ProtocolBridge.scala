package com.github.bsnisar

import org.json4s.JValue

import scala.util.Try

trait ProtocolBridge {

  /**
    * Application level protocol from some web service. It is represented by specific json payload structure.
    * @param json income json
    * @return payload
    */
  def compute(json: JValue): Try[JValue]
}
