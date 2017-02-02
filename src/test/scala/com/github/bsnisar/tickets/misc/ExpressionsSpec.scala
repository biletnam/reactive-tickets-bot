package com.github.bsnisar.tickets.misc

import org.scalatest.FunSuite

class ExpressionsSpec extends FunSuite {
  import Expressions._
  import scala.language.dynamics

  test("match starts with") {
    val str = "Hello world"
    str match {
      case Expr.Hello.StartsWith(fn) =>
    }
  }
}
