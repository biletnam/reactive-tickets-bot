package com.github.bsnisar.tickets

import org.jmock.Mockery
import org.jmock.lib.concurrent.Synchroniser
import org.scalatest.{Assertions, BeforeAndAfter, BeforeAndAfterAll, FlatSpecLike}

/**
  * Base test class.
  */
trait BaseTest extends FlatSpecLike with BeforeAndAfterAll with BeforeAndAfter with Assertions {
  def newMockery(): Mockery = new Mockery {{
    setThreadingPolicy(new Synchroniser)
  }}
}
