package edu.psu.core;

import edu.psu.behavior.TicketStateIF;

import java.time.Instant;
import java.util.UUID;

public class Ticket {
    // fields
    private UUID ticketID;
    private Instant createdAt;
    private TicketStateIF status;
    private String assignee;
    private String title;
    private String description;
    private int priority;

    // constructors
    public Ticket(UUID id, String title, Instant createdAt, int priority) {}

    // unimplemented methods
    public void processEvent(int event) {};
    public String displayDetails() {return null;}

    // setters
    public void setState(TicketStateIF state) {status = state;}

    // getters
    public Instant getCreatedAt() {return createdAt;}
    public int getPriority() {return priority;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}

}
