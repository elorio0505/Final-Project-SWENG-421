package edu.psu.core;

import edu.psu.behavior.TicketStateIF;

public interface TicketComponentIF {
    // Control Methods
    String displayDetails();
    void processEvent(int event);

    // Getters
    int getPriority();
    String getTitle();
    String getDescription();

    // Setters
    void setPriority(int priority);
    void setState(TicketStateIF state);
}
