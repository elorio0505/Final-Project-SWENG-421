package edu.psu.core;

public abstract class AbsTicketDecorator implements TicketComponentIF {
    protected TicketComponentIF decoratedComponent;

    public AbsTicketDecorator(TicketComponentIF c) {}

    @Override
    public String getTitle() {return "";}

    @Override
    public String getDescription() {return "";}

    @Override
    public String displayDetails() {return "";}

}
