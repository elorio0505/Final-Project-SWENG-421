package edu.psu.core;

import edu.psu.behavior.NewState;
import edu.psu.behavior.TicketStateIF;

import java.time.Instant;
import java.util.UUID;

public class Ticket implements TicketComponentIF {
    // fields
    private final UUID ticketID;
    private final Instant createdAt;
    private final String title;
    private final String description;
    private TicketStateIF status;
    private String assignee;
    private int priority;

    // TODO: Ticket creation logic should be modified by the processing package, so this is likely to change heavily
    // constructors
    public Ticket(UUID id, String title, int priority) {
        ticketID = id;
        createdAt = Instant.now();
        // TODO: I'm sure you want to handle this differently, change when state behavior is implemented
        status = new NewState();
        this.title = title;
        this.priority = priority;
        this.assignee = "TEMP";
        this.description = "TEMP";
    }

    public void processEvent(int event) {}

    public String displayDetails() {
        return "UUID: " + ticketID +
                "\nTicket Title: " + title +
                "\nTime Created: " + createdAt +
                "\nCurrent Status: " + status +
                "\nAsigneee: " + assignee +
                "\nDescription: " + description +
                "\nPriority: " + priority;
    }

    // setters
    public void setState(TicketStateIF state) {status = state;}
    public void setPriority(int priority) {this.priority = priority;}

    // getters
    public int getPriority() {return priority;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}

}
