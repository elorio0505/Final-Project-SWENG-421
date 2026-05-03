package edu.psu.core;

import edu.psu.behavior.AbsTicketState;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Ticket implements TicketComponentIF {
    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final UUID ticketID;
    private final Instant createdAt;
    private final String title;
    private final String description;
    private AbsTicketState state;
    private String assignee;
    private String department;
    private int priority;

    //activity log
    private final List<String> activityLog = new ArrayList<>();

    // Contructors

    //for builders
    public Ticket(UUID id, String title, String description, int priority) {
        this(id, title, description, priority, Instant.now());
    }

    //for StorageManager on load
    public Ticket(UUID id, String title, String description, int priority, Instant createdAt) {
        this.ticketID    = id;
        this.title       = title;
        this.description = description;
        this.priority    = priority;
        this.createdAt   = createdAt;
        this.state       = AbsTicketState.start();
        this.assignee    = "Unassigned";
        this.department  = "Unassigned";
    }

    // Methods
    @Override
    public void processEvent(int event) {
        AbsTicketState oldState = this.state;
        state.processEvent(this, event);
        if (oldState != this.state) {
            addLog("State changed: " + oldState + " → " + this.state);
            IncidentRegistry.syncStorage();
        } else {
            System.out.println("Invalid transition: Event " + event + " not allowed in " + oldState);
        }
    }

    public void addLog(String note) {
        activityLog.add("[" + FMT.format(Instant.now()) + "] " + note);
    }

    public List<String> getActivityLog() {
        return activityLog;
    }

    @Override
    public String displayDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID:         ").append(ticketID).append("\n");
        sb.append("Title:        ").append(title).append("\n");
        sb.append("Created:      ").append(FMT.format(createdAt)).append("\n");
        sb.append("Status:       ").append(state).append("\n");
        sb.append("Priority:     ").append(priority).append("\n");
        sb.append("Department:   ").append(department).append("\n");
        sb.append("Assignee:     ").append(assignee).append("\n");
        sb.append("Description:  ").append(description).append("\n");
        return sb.toString();
    }

    // Setters
    public void setState(AbsTicketState state) { this.state = state; }
    public void setPriority(int priority)       { this.priority = priority; }
    public void setAssignee(String assignee)    { this.assignee = assignee; }
    public void setDepartment(String dept)      { this.department = dept; }

    // Getters
    public int getPriority()        { return priority; }
    public Instant getCreatedAt()   { return createdAt; }
    public AbsTicketState getState(){ return state; }
    public UUID getTicketID()       { return ticketID; }
    public String getAssignee()     { return assignee; }
    public String getDepartment()   { return department; }
    public String getTitle()        { return title; }
    public String getDescription()  { return description; }

    @Override
    public String toString() {
        return "[" + state + "] " + title;
    }
}