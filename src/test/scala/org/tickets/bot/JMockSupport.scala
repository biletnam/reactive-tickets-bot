package org.tickets.bot

import org.jmock.api.Action
import org.jmock.{AbstractExpectations, Expectations, Mockery}
import org.jmock.lib.concurrent.Synchroniser
import org.jmock.lib.legacy.ClassImposteriser
import org.scalatest.jmock.JMockExpectations

import scala.reflect.ClassTag

object JMockSupport {

  def threadSafe: JMockSupport = {
    val mockery = new Mockery
    mockery.setImposteriser(ClassImposteriser.INSTANCE)
    mockery.setThreadingPolicy(new Synchroniser)
    new JMockSupport(mockery)
  }
}

class JMockSupport(val mockery: Mockery) {

  def mock[T <: AnyRef](implicit classTag: ClassTag[T]): T = {
    mockery.mock(classTag.runtimeClass.asInstanceOf[Class[T]])
  }

  class Expects extends Expectations {
    def returnValue(obj: Any): Action = AbstractExpectations.returnValue(obj)
  }

  /**
    * See [[org.scalatest.jmock.JMockCycle]]
    */
  def expecting(fun: Expects => Unit): Unit = {
    val e = new Expects
    fun(e)
    mockery.checking(e)
  }
}
