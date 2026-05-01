package edu.psu.processing;

import java.util.Map;

public class WebFormSource implements ParsingStrategyIF {
    public WebFormSource() {}

    @Override
    public void parseRaw(Object data, AbsTicketBuilder builder) {
        Map<?, ?> ticketData = TicketDataParser.requireMap(data, "Web form");

        String title = TicketDataParser.requireString(ticketData, "title", "Web form");
        String description = TicketDataParser.requireString(ticketData, "description", "Web form");
        int priority = TicketDataParser.requireInt(ticketData, "priority", "Web form");

        builder.setBasics(title, description);
        builder.setMetaData("Web Form", priority);
    }
}
