package com.github.bsnisar.tickets

import org.jmock.{AbstractExpectations, Expectations}
import org.jmock.api.Action

trait JMockExpectations extends Expectations {
  def returnValue (result: Any): Action = AbstractExpectations.returnValue(result)
}
