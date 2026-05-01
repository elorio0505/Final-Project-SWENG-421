package edu.psu.processing;

import edu.psu.core.*;

import java.time.Instant;

public class SoftwareTicketBuilder extends AbsTicketBuilder {
    private Ticket resultTicket;

    public SoftwareTicketBuilder() {};

    @Override
    public void setBasics(String title, String desc) {
        this.title = title;
        this.description = desc;
    }

    @Override
    public void setMetaData(String source, int priority) {
        this.source = source;
        this.priority = priority;
        this.createdAt = Instant.now();
    }

    @Override
    public TicketComponentIF getProduct() {return null;}
}
