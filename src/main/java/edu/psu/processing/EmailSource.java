package edu.psu.processing;

import java.util.Map;

public class EmailSource extends AbsTicketSource implements ParsingStrategyIF {

    public EmailSource() {
        super(null);
        this.setStrategy(this);
    }

    @Override
    public void parseRaw(Object data, AbsTicketBuilder builder) {
        Map<?, ?> emailData = TicketDataParser.requireMap(data, "Email");

        String title = TicketDataParser.requireString(emailData, "subject", "Email");
        String description = TicketDataParser.requireString(emailData, "body", "Email");
        int priority = TicketDataParser.requireInt(emailData, "priority", "Email");

        builder.setBasics(title, description);
        builder.setMetaData("Email", priority);
    }
}