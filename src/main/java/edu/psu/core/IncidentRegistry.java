package edu.psu.core;

import java.util.ArrayList;
import java.util.List;

public class IncidentRegistry {
    private static final List<IncidentComposite> incidentList = new ArrayList<>();

    /**
     * Adds a new incident to the registry
     * @param incidentTitle Title of the new incident
     * @param incidentDescription Description of the new incident
     * @return Reference to the new incident
     */
    public static IncidentComposite newIncident(String incidentTitle, String incidentDescription)  {
        IncidentComposite newIncident = new IncidentComposite(incidentTitle, incidentDescription);
        incidentList.add(newIncident);
        return newIncident;
    }

    /**
     * Get the incidents that contain a given ticket component
     * @param c Ticket component to look for
     * @return List of incidents that contain the input component
     */
    public static List<IncidentComposite> getContainingIncidents(TicketComponentIF c) {
        List<IncidentComposite> list = new ArrayList<>();
        for (IncidentComposite i : incidentList) {
            if (i.getChildren().contains(c)) {
                list.add(i);
            }
        }
        return list;
    }

    /**
     * Replaces one component with another in all incidents
     * Used to update references of newly-wrapped components
     * @param oldTC The old ticket component to be replaced
     * @param newTC The new ticket component to replace the old one
     */
    public static void replaceInAllIncidents(TicketComponentIF oldTC, TicketComponentIF newTC) {
        List<IncidentComposite> existsIn = getContainingIncidents(oldTC);
        for (IncidentComposite i : existsIn) {
            i.replaceChild(oldTC, newTC);
        }
    }
}
