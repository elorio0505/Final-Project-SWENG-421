package edu.psu.core;

import edu.psu.behavior.AbsTicketState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public abstract class AbsTicketDecorator implements TicketComponentIF {
    public TicketComponentIF decoratedComponent;

    // Constructor
    public AbsTicketDecorator(TicketComponentIF c) {
        this.decoratedComponent = c;
        IncidentRegistry.replaceInAllIncidents(c, this);
    }

    // Methods
    public String displayDetails()          { return decoratedComponent.displayDetails(); }
    public void processEvent(int event)     { decoratedComponent.processEvent(event); }
    public AbsTicketState getState()        { return decoratedComponent.getState(); }
    
    // Setters
    public void setState(AbsTicketState s)  { decoratedComponent.setState(s); }
    public void setPriority(int p)          { decoratedComponent.setPriority(p); }
    public void setAssignee(String a)       { decoratedComponent.setAssignee(a); }
    
    // Getters
    public String getTitle()                { return decoratedComponent.getTitle(); }
    public String getDescription()          { return decoratedComponent.getDescription(); }
    public int getPriority()                { return decoratedComponent.getPriority(); }
    public Instant getCreatedAt()           { return decoratedComponent.getCreatedAt(); }
    public UUID getTicketID()               { return decoratedComponent.getTicketID(); }
    public String getAssignee()             { return decoratedComponent.getAssignee(); }
    public void addLog(String note)         { decoratedComponent.addLog(note); }
    public List<String> getActivityLog()    { return decoratedComponent.getActivityLog(); }
}
