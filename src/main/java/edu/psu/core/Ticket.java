package edu.psu.core;

import edu.psu.behavior.NewState;
import edu.psu.behavior.TicketStateIF;

import java.time.Instant;
import java.util.UUID;

public class Ticket implements TicketComponentIF {
    private final UUID ticketID;
    private final Instant createdAt;
    private final String title;
    private final String description;
    private TicketStateIF state;
    private String assignee;
    private int priority;

    // TODO: Ticket creation logic should be modified by the processing package, so this is likely to change heavily
    // Constructors
    public Ticket(UUID id, String title, int priority) {
        ticketID = id;
        createdAt = Instant.now();
        // TODO: I'm sure you want to handle this differently, change when state behavior is implemented
        state = new NewState();
        this.title = title;
        this.priority = priority;
        this.assignee = "TEMP";
        this.description = "TEMP";
    }

    // Methods
    public void processEvent(int event) {
        // TODO: Event processing logic
    }

    public String displayDetails() {
        return "UUID: " + ticketID +
                "\nTicket Title: " + title +
                "\nTime Created: " + createdAt +
                "\nCurrent Status: " + state +
                "\nAsigneee: " + assignee +
                "\nDescription: " + description +
                "\nPriority: " + priority;
    }

    // setters
    public void setState(TicketStateIF state) {this.state = state;}
    public void setPriority(int priority) {this.priority = priority;}
    public void setAssignee(String assignee) {this.assignee = assignee;}

    // getters
    public int getPriority() {return priority;}
    public Instant getCreatedAt() {return createdAt;}
    public TicketStateIF getState() {return state;}
    public UUID getTicketID() {return ticketID;}
    public String getAssignee() {return assignee;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}

}
