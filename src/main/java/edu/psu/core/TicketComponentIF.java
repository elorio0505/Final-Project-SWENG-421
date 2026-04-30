package edu.psu.core;

import edu.psu.behavior.TicketStateIF;

import java.time.Instant;
import java.util.UUID;

public interface TicketComponentIF {
    // Control Methods
    String displayDetails();
    void processEvent(int event);

    // Getters
    String getTitle();
    String getDescription();
    int getPriority();
    Instant getCreatedAt();
    TicketStateIF getState();
    UUID getTicketID();
    String getAssignee();

    // Setters
    void setAssignee(String assignee);
    void setPriority(int priority);
    void setState(TicketStateIF state);
}
