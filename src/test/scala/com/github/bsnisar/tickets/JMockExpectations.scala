package com.github.bsnisar.tickets

import org.hamcrest.Matchers
import org.jmock.api.Action
import org.jmock.{AbstractExpectations, Expectations}


trait JMockExpectations extends Expectations {
  import java.lang.{Class => jClass}

  import scala.reflect._

  def returnValue (result: Any): Action = AbstractExpectations.returnValue(result)
  def any[T: ClassTag]: T = {
    val clazz: jClass[T] = classTag[T].runtimeClass.asInstanceOf[jClass[T]]
    `with`(Matchers.any(clazz))
  }
}
