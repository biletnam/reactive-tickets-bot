package com.github.bsnisar.tickets

/**
  * Created by bsnisar on 05.05.17.
  */
case class Event[E](payload: E, headers: Map[String, String])
