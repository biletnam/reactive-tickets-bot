package com.github.bsnisar.tickets.misc

import java.io.StringWriter
import java.util.Locale

import com.github.bsnisar.tickets.telegram.Msg
import com.typesafe.scalalogging.LazyLogging
import freemarker.template.{Configuration, Version}

trait Templates {
  def renderMsg(msg: Msg): String
}

class TemplatesFreemarker extends Templates  with LazyLogging {
  // scalastyle:off magic.number
  val cfg = new Configuration(new Version(2, 3, 20))
  // Where do we load the templates from:
  cfg.setClassForTemplateLoading(classOf[TemplatesFreemarker], "/templates")
  // Some other recommended settings:
  cfg.setDefaultEncoding("UTF-8")
  cfg.setLocale(Locale.US)

  override def renderMsg(msg: Msg): String = {
    logger.debug(s"getting template ${msg.id}, local ${msg.local}, params ${msg.params}")
    val template = cfg.getTemplate(s"${msg.id.name}.ftl", msg.local)
    val writer = new StringWriter()
    template.process(msg.params, writer)
    writer.toString
  }
}
