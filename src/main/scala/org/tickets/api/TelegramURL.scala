package org.tickets.api

import com.typesafe.config.Config

/**
  * Telegram API url.
  */
class TelegramURL(val cfg: Config) {
  private lazy val baseUrl: String = s"https://api.telegram.org/bot${cfg.getConfig("bot.api.token")}"
  lazy val getMe = s"$baseUrl/getMe"
  lazy val getUpdates = s"$baseUrl/getUpdates"
  lazy val sendMessage = s"$baseUrl/sendMessage"
}
