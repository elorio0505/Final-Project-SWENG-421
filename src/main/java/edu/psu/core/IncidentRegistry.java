package edu.psu.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import edu.psu.processing.StorageManager;
import edu.psu.processing.StorageManager.LoadResult;

public class IncidentRegistry {
    private static final List<IncidentComposite> incidentList = new ArrayList<>();
    private static final List<TicketComponentIF> allTickets   = new ArrayList<>();
    private static final StorageManager storage = new StorageManager();
    private static boolean loaded = false;

    /**
     * Load from file at startup
     */
    public static void syncStorage() {
        if (!loaded) {
            LoadResult result = storage.loadAll();
            allTickets.addAll(result.tickets);
            incidentList.addAll(result.incidents);
            loaded = true;
        }
        // Save both tickets and incidents every time
        storage.saveAll(allTickets, incidentList);
    }

    public static void registerTicket(TicketComponentIF ticket) {
        if (!allTickets.contains(ticket)) {
            allTickets.add(ticket);
            syncStorage();
        }
    }

    public static List<TicketComponentIF> getAllTickets() {
        return new ArrayList<>(allTickets);
    }

    public static List<IncidentComposite> getAllIncidents() {
        return new ArrayList<>(incidentList);
    }

    /**
     * for not nested tickets
     */
    public static List<TicketComponentIF> getStandaloneTickets() {
        Set<TicketComponentIF> nested = incidentList.stream()
            .flatMap(i -> i.getChildren().stream())
            .collect(Collectors.toSet());
        return allTickets.stream()
            .filter(t -> !nested.contains(t))
            .collect(Collectors.toList());
    }
    
    /**
     * Adds a new incident to the registry
     * @param incidentTitle Title of the new incident
     * @param incidentDescription Description of the new incident
     * @return Reference to the new incident
     */
    public static IncidentComposite newIncident(String title, String description) {
        IncidentComposite inc = new IncidentComposite(title, description);
        incidentList.add(inc);
        syncStorage();
        return inc;
    }

    /**
     * Get the incidents that contain a given ticket component
     * @param c Ticket component to look for
     * @return List of incidents that contain the input component
     */
    public static List<IncidentComposite> getContainingIncidents(TicketComponentIF c) {
        List<IncidentComposite> list = new ArrayList<>();
        for (IncidentComposite i : incidentList) {
            if (i.getChildren().contains(c)) list.add(i);
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
        int index = allTickets.indexOf(oldTC);
        if (index != -1) allTickets.set(index, newTC);
        for (IncidentComposite i : getContainingIncidents(oldTC)) {
            i.replaceChild(oldTC, newTC);
        }
        syncStorage();
    }
}
