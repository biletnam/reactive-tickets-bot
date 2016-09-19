package org.tickets.msg.telegram;

public interface TgUpdate {
    long chatID();
    String text();
}
