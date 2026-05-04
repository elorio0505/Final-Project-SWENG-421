package edu.psu.core;

import edu.psu.behavior.AbsTicketState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IncidentComposite implements TicketComponentIF {
    private final List<TicketComponentIF> children = new ArrayList<>();
    private final String incidentTitle;
    private final String incidentDescription;

    // Constructor
    public IncidentComposite(String incidentTitle, String incidentDescription) {
        this.incidentTitle = incidentTitle;
        this.incidentDescription = incidentDescription;
    }

    // Methods
    public void addChild(TicketComponentIF c) {
        if (c == this) return; // cannot add an incident to itself
        if (!children.contains(c)) {
            children.add(c);
        }
    }

    public void remove(TicketComponentIF c) {
        children.remove(c);
    }

    /**
     * Replace a child with another child, must be called when creating a decorator.
     * @param oldTC Old TicketComponent
     * @param newTC New TicketComponent
     */
    public void replaceChild(TicketComponentIF oldTC, TicketComponentIF newTC) {
        int index = children.indexOf(oldTC);
        if (index != -1) {
            children.set(index, newTC);
        }
    }

    /**
     * Returns readable version of incident properties
     * @return String of formatted information about the incident
     */
    @Override
    public String displayDetails() {
        return "Title: " + incidentTitle +
                "\nDescription: " + incidentDescription +
                "\nChild Ticket Count: " + children.size();
    }

    /**
     * Attempt an event transition for all the incident's children.
     * @param event integer corresponding to the event executed
     */
    @Override
    public void processEvent(int event) {
        for (TicketComponentIF c : children) {
            c.processEvent(event);
        }
    }

    // Getters
    public int getPriority() {
        return -1;
    }
    public Instant getCreatedAt() {return null;}
    public AbsTicketState getState() {return null;}
    public UUID getTicketID() {return null;}
    public String getAssignee() {return "";}
    public List<TicketComponentIF> getChildren() {
        return children;
    }
    public String getTitle() {
        return incidentTitle;
    }
    public String getDescription() {
        return incidentDescription;
    }

    // Setters
    /**
     * Set priority of all incident's children
     * @param priority value to set priority to
     */
    @Override
    public void setPriority(int priority) {
        for (TicketComponentIF c : children) {
            c.setPriority(priority);
        }
    }

    /**
     * Sets all of this incident's children to the same assignee
     * @param assignee Assignee to assign
     */
    public void setAssignee(String assignee) {
        for (TicketComponentIF c : children) {
            c.setAssignee(assignee);
        }
    }

    /**
     * Sets the state of all children to the given state
     * @param state New state
     */
    @Override
    public void setState(AbsTicketState state) {
        for (TicketComponentIF c : children) {
            c.setState(state);
        }

    }
    
    public String toString() {
    return this.getTitle();
    }
}