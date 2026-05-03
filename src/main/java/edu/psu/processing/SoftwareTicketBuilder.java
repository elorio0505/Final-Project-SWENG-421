package edu.psu.processing;

import edu.psu.core.*;
import java.util.UUID;
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
    public TicketComponentIF getProduct() {
        UUID id = UUID.randomUUID();
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        TicketComponentIF t = new Ticket(id, title, description, priority, createdAt);
        IncidentRegistry.registerTicket(t); 
        return t;
    }
}