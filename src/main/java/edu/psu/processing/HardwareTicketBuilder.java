package edu.psu.processing;

import edu.psu.core.TicketComponentIF;

public class HardwareTicketBuilder extends AbsTicketBuilder {
    public HardwareTicketBuilder() {}

    @Override
    public void setBasics(String title, String desc) {}

    @Override
    public void setMetaData(String source, int priority) {}

    @Override
    public TicketComponentIF getProduct() {return null;}
}
