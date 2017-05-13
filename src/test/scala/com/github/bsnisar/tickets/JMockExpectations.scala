package com.github.bsnisar.tickets

import org.hamcrest.{Description, DiagnosingMatcher, Matchers}
import org.jmock.api.Action
import org.jmock.{AbstractExpectations, Expectations}


trait JMockExpectations extends Expectations {
  import java.lang.{Class => jClass}

  import scala.reflect._

  def returnValue (result: Any): Action = AbstractExpectations.returnValue(result)

  def matchPF[T](m: PartialFunction[Any, Boolean]): T = {
    `with`(new DiagnosingMatcher[T] {
      override def matches(item: scala.Any, mismatch: Description): Boolean = {
        if (!m.isDefinedAt(item)) {
          mismatch.appendValue(item).appendText(" not defined at partial function")
          false
        } else {
          val res = m(item)

          if (res) true else {
            mismatch.appendValue(item).appendText(" not match at partial function")
            false
          }
        }
      }

      override def describeTo(description: Description): Unit = {
        description.appendText("match partial funtion")
      }
    })
  }

  def any[T: ClassTag]: T = {
    val clazz: jClass[T] = classTag[T].runtimeClass.asInstanceOf[jClass[T]]
    `with`(Matchers.any(clazz))
  }
}
