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

    public abstract void setBasics(String title, String desc);
    public abstract void setMetaData(String source, int priority);
    public abstract TicketComponentIF getProduct();

    public void reset() {}

}
