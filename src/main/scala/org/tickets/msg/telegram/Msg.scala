package org.tickets.msg.telegram

import com.fasterxml.jackson.annotation.JsonProperty

/**
  * Telegram Message.
  */
case class Msg(@JsonProperty("message_id") id: Long,
               @JsonProperty("user") user: User,
               @JsonProperty("chat") chat: Chat,
               @JsonProperty("text") text: Option[String])

case class User(
                 @JsonProperty("user_id") id: Int,
                 @JsonProperty("first_name") fName: String,
                 @JsonProperty("last_name") lName: Option[String],
                 @JsonProperty("username") name: Option[String])


case class Chat(@JsonProperty("chat_id") id: Long)

