package edu.psu.core;

import edu.psu.behavior.AbsTicketState;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TicketComponentIF {
    //Methods
    String displayDetails();
    void processEvent(int event);

    // Getters
    String getTitle();
    String getDescription();
    int getPriority();
    Instant getCreatedAt();
    AbsTicketState getState();
    UUID getTicketID();
    String getAssignee();

    // Setters
    void setAssignee(String assignee);
    void setPriority(int priority);
    void setState(AbsTicketState state);

    // Activity log (default no-op for composites/decorators that delegate down)
    default void addLog(String note) {}
    default List<String> getActivityLog() { return java.util.Collections.emptyList(); }
}
