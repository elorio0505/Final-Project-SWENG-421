package edu.psu.core;

import java.util.List;

public class IncidentComposite implements TicketComponentIF {
    private List<TicketComponentIF> components;
    private String incidentTitle;

    public IncidentComposite() {};

    public void add(TicketComponentIF c) {}
    public void remove(TicketComponentIF c) {}
    public List<TicketComponentIF> getChildren() {return null;}

    @Override
    public String getTitle() {return incidentTitle;}

    @Override
    public String getDescription() {return "";}

    @Override
    public String displayDetails() {return "";}
}
