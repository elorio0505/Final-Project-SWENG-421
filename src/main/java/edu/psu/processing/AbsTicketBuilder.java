package edu.psu.processing;

import edu.psu.core.TicketComponentIF;

import java.time.Instant;

public abstract class AbsTicketBuilder {
    protected Instant createdAt;
    protected String title;
    protected String description;
    protected int priority;
    protected String source;

    public AbsTicketBuilder() {}

    public void setBasics(String title, String desc) {
        this.title = title;
        this.description = desc;
    }

    public void setMetaData(String source, int priority) {
        this.source = source;
        this.priority = priority;
        this.createdAt = Instant.now();
    }

    public abstract TicketComponentIF getProduct();

    public void reset() {
        createdAt = null;
        title = null;
        description = null;
        priority = 0;
        source = null;
    }

}
