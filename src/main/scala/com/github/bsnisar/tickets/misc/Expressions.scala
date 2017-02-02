package com.github.bsnisar.tickets.misc



object Expressions {


  class RegexpExtractor(params: Seq[String]) {
    def unapplySeq(str: String): Option[Seq[String]] =
      params.headOption flatMap (_.r unapplySeq str)
  }

  class StartsWithExtractor(params: Seq[String]) {
    def unapply(str: String): Option[String] =
      params.headOption filter (str startsWith _) map (_ => str)
  }

  import scala.language.dynamics

  class ExtractorParams(params: Seq[String]) extends Dynamic {
    val StartsWith = new StartsWithExtractor(params)
    val Regexp = new RegexpExtractor(params)

    def selectDynamic(name: String): ExtractorParams =
      new ExtractorParams(params :+ name)
  }

  object Expr extends ExtractorParams(Nil)
}
