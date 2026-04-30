package edu.psu.core;

import edu.psu.behavior.TicketStateIF;

public abstract class AbsTicketDecorator implements TicketComponentIF {
    protected TicketComponentIF decoratedComponent;

    // Constructors
    public AbsTicketDecorator(TicketComponentIF c) {
        this.decoratedComponent = c;
        IncidentRegistry.replaceInAllIncidents(c, this);
    }

    // Methods
    @Override
    public String displayDetails() {
        return decoratedComponent.displayDetails();
    }

    @Override
    public void processEvent(int event) {
        decoratedComponent.processEvent(event);
    }

    // Setters
    @Override
    public void setState(TicketStateIF state) {
        decoratedComponent.setState(state);
    }

    @Override
    public void setPriority(int priority) {
        decoratedComponent.setPriority(priority);
    }

    // Getters
    @Override
    public String getTitle() {
        return decoratedComponent.getTitle();
    }

    @Override
    public String getDescription() {
        return decoratedComponent.getDescription();
    }
    @Override
    public int getPriority() {
        return decoratedComponent.getPriority();
    }
}
