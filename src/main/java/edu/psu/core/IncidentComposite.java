package edu.psu.core;

import edu.psu.behavior.TicketStateIF;

import java.util.ArrayList;
import java.util.List;

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
    public void add(TicketComponentIF c) {
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
     * Returns human-readable version of incident properties
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
    /**
     * Since individual tickets in an incident may all have different priorities, this returns -1 to represent invalid
     * @return -1
     */
    @Override
    public int getPriority() {
        return -1;
    }

    public List<TicketComponentIF> getChildren() {
        return children;
    }

    @Override
    public String getTitle() {
        return incidentTitle;
    }

    @Override
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
     * Sets the state of all children to the given state
     * @param state New state
     */
    @Override
    public void setState(TicketStateIF state) {
        for (TicketComponentIF c : children) {
            c.setState(state);
        }

    }
}