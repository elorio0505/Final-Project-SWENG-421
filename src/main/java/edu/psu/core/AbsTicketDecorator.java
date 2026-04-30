package edu.psu.core;

import edu.psu.behavior.TicketStateIF;

import java.time.Instant;
import java.util.UUID;

public abstract class AbsTicketDecorator implements TicketComponentIF {
    protected TicketComponentIF decoratedComponent;

    // Constructors
    public AbsTicketDecorator(TicketComponentIF c) {
        this.decoratedComponent = c;
        IncidentRegistry.replaceInAllIncidents(c, this);
    }

    // Methods
    public String displayDetails() {return decoratedComponent.displayDetails();}
    public void processEvent(int event) {decoratedComponent.processEvent(event);}

    // Setters
    public void setState(TicketStateIF state) {decoratedComponent.setState(state);}
    public void setPriority(int priority) {decoratedComponent.setPriority(priority);}
    public void setAssignee(String assignee) {decoratedComponent.setAssignee(assignee);}

    // Getters
    public String getTitle() {return decoratedComponent.getTitle();}
    public String getDescription() {return decoratedComponent.getDescription();}
    public int getPriority() {return decoratedComponent.getPriority();}
    public Instant getCreatedAt() {return decoratedComponent.getCreatedAt();}
    public TicketStateIF getState() {return decoratedComponent.getState();}
    public UUID getTicketID() {return decoratedComponent.getTicketID();}
    public String getAssignee() {return decoratedComponent.getAssignee();}
}