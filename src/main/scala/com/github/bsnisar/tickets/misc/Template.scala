package com.github.bsnisar.tickets.misc

import java.io.StringWriter
import java.util.Locale

import com.github.bsnisar.tickets.telegram.TelegramMessages.SendMsg
import com.typesafe.scalalogging.LazyLogging
import freemarker.template.{Configuration, Version}

trait Template {
  def eval(msg: SendMsg): String
}

class TemplateFreemarker extends Template  with LazyLogging {
  // scalastyle:off magic.number
  val cfg = new Configuration(new Version(2, 3, 20))
  // Where do we load the templates from:
  cfg.setClassForTemplateLoading(classOf[TemplateFreemarker], "/templates")
  // Some other recommended settings:
  cfg.setDefaultEncoding("UTF-8")
  cfg.setLocale(Locale.US)

  override def eval(msg: SendMsg): String = {
    logger.debug(s"getting template ${msg.id}, local ${msg.local}, params ${msg.params}")
    val template = cfg.getTemplate(s"${msg.id.name}.ftl", msg.local)
    val writer = new StringWriter()
    template.process(msg.params, writer)
    writer.toString
  }
}
