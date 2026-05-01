package edu.psu.processing;

import java.util.Map;

class TicketDataParser {
    private TicketDataParser() {}

    static Map<?, ?> requireMap(Object data, String sourceName) {
        if (!(data instanceof Map<?, ?> ticketData)) {
            throw new IllegalArgumentException(sourceName + " ticket data must be provided as a map.");
        }
        return ticketData;
    }

    static String requireString(Map<?, ?> data, String key, String sourceName) {
        Object value = data.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(sourceName + " ticket data must include " + key + ".");
        }
        return value.toString();
    }

    static int requireInt(Map<?, ?> data, String key, String sourceName) {
        Object value = data.get(key);
        if (value == null) {
            throw new IllegalArgumentException(sourceName + " ticket data must include " + key + ".");
        }

        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(sourceName + " ticket " + key + " must be a number.", e);
        }
    }
}
