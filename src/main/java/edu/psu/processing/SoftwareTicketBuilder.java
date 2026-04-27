package edu.psu.processing;

import edu.psu.core.*;

public class SoftwareTicketBuilder extends AbsTicketBuilder {
    private Ticket resultTicket;

    public SoftwareTicketBuilder() {};

    @Override
    public void setBasics(String title, String desc) {}

    @Override
    public void setMetaData(String source, int priority) {}

    @Override
    public TicketComponentIF getProduct() {return null;}
}
