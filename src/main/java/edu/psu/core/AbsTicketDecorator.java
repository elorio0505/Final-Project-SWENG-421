package edu.psu.core;

public abstract class AbsTicketDecorator implements TicketComponentIF {
    protected TicketComponentIF decoratedComponent;

    public AbsTicketDecorator(TicketComponentIF c) {}

    @Override
    public String getTitle() {
        return decoratedComponent.getTitle();
    }

    @Override
    public String getDescription() {
        return decoratedComponent.getDescription();
    }

    @Override
    public String displayDetails() {
        return decoratedComponent.displayDetails();
    }

}
