package org.tickets.msg.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

public final class TgUpdateJsonNode implements TgUpdate {
    private final long chatId;
    private final String text;

    public TgUpdateJsonNode(JsonNode node) {
        JsonNode message = node.findValue("message");
        this.text = message.findValue("text").asText();
        this.chatId = message.findValue("chat").get("id").asLong();
    }

    @Override
    public long chatID() {
        return chatId;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("chatID", chatId)
                .add("text", text)
                .toString();
    }
}
