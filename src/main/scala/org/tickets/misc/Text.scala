package org.tickets.misc

import java.text.MessageFormat
import java.util.ResourceBundle

object Text {
  val Separator = "\n"
  val Bundle = ResourceBundle.getBundle("Messages")

  def bundle(key: BundleKey): String = Bundle.getString(key.name)
  def bundle(key: BundleKey, arg: AnyRef): String = MessageFormat.format(Bundle.getString(key.name), arg)

}

class Text(private val buff: StringBuilder = new StringBuilder) {

  def addBundle(key: BundleKey, arg: AnyRef) = {
    addLine(Text.bundle(key, arg))
    this
  }

  def addBundle(key: BundleKey) = {
    addLine(Text.bundle(key))
    this
  }

  def addLine(str: String) = {
    buff.append(str)
    newLine
    this
  }

  def withDashes: Text = {
    buff.append("----------")
    newLine
    this
  }

  def newLine: Text = {
    buff.append(Text.Separator)
    this
  }

  def mkString: String = buff.mkString

}
