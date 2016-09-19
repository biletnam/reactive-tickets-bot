package org.tickets.msg.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.Iterator;
import java.util.List;

public class TgUpdatesJsonNode implements TgUpdates {
    private final List<TgUpdate> updates;

    public TgUpdatesJsonNode(JsonNode node) {
        ArrayNode arrayNode = (ArrayNode) node;
        updates = ImmutableList.copyOf(Iterables.transform(arrayNode, TgUpdateJsonNode::new));
        System.out.println(updates);
    }

    @Override
    public Iterator<TgUpdate> iterator() {
        return updates.iterator();
    }
}
