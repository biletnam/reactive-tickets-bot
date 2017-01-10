package com.github.bsnisar.tickets

import org.scalatest.{Assertions, BeforeAndAfterAll, FlatSpecLike}

/**
  * Base test class.
  */
trait BaseTest extends FlatSpecLike with BeforeAndAfterAll with Assertions
