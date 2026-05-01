package edu.psu.processing;

import java.util.Map;

public class ManualSource implements ParsingStrategyIF{
    public ManualSource() {}

    @Override
    public void parseRaw(Object data, AbsTicketBuilder builder) {
        Map<?, ?> ticketData = TicketDataParser.requireMap(data, "Manual");

        String title = TicketDataParser.requireString(ticketData, "title", "Manual");
        String description = TicketDataParser.requireString(ticketData, "description", "Manual");
        int priority = TicketDataParser.requireInt(ticketData, "priority", "Manual");

        builder.setBasics(title, description);
        builder.setMetaData("Manual", priority);
    }
}
