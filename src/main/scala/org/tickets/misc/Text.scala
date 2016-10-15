package org.tickets.misc

object Text {
  val Separator = "\n"
}

class Text(private val buff: StringBuilder = new StringBuilder) {

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
