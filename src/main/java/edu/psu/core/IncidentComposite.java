package edu.psu.core;

import java.util.List;

public class IncidentComposite implements TicketComponentIF {
    private List<TicketComponentIF> components;
    private String incidentTitle;
    private String incidentDescription;

    public IncidentComposite(String incidentTitle, String incidentDescription) {
        this.incidentTitle = incidentTitle;
        this.incidentDescription = incidentDescription;
    }

    public void add(TicketComponentIF c) {
        components.add(c);
    }

    public void remove(TicketComponentIF c) {
        components.remove(c);
    }

    public List<TicketComponentIF> getChildren() {
        return components;
    }

    @Override
    public String getTitle() {
        return incidentTitle;
    }

    @Override
    public String getDescription() {
        return incidentDescription;
    }

    // TODO: figure out what this is supposed to be for
    @Override
    public String displayDetails() {
        return "";
    }
}
