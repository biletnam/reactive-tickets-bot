package org.tickets.bot.uz

import java.util.regex.Pattern

import org.tickets.api.token.JJEncoder

/**
  * Extract token from page content.
  * @author bsnisar
  */
private object UzToken {
  val EncodedDataPattern = Pattern.compile("\\$\\$_=.*~\\[\\];.*\"\"\\)\\(\\)\\)\\(\\);")
  val TokenPattern = Pattern.compile("[0-9a-f]{32}")


  def apply(content: String): String = {
    val matcher = UzToken.EncodedDataPattern.matcher(content)
    require(matcher.find(), "Encoded block not found")

    val encodedTokenData: String = content.substring(matcher.start, matcher.end)
    val decodedTokenData: String = new JJEncoder().decode(encodedTokenData)
    val tokenMatcher = UzToken.TokenPattern.matcher(decodedTokenData)
    require(tokenMatcher.find(), "Token not found")

    val token: String = decodedTokenData.substring(matcher.start, matcher.end)
    token
  }
}

