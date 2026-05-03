package edu.psu.processing;

import java.util.Map;

public class ManualSource extends AbsTicketSource implements ParsingStrategyIF {
    public ManualSource() {
        super(null); 
        this.setStrategy(this);
    }

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
