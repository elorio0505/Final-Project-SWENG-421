package edu.psu.processing;

import edu.psu.core.HardwareTicket;
import edu.psu.core.Ticket;
import edu.psu.core.TicketComponentIF;
import edu.psu.core.IncidentComposite;
import java.io.*;
import java.time.Instant;
import java.util.*;

/**
 * Persists the full system state to ticket_storage.txt.
 * File format:
 *   TICKETS
 *   SW|UUID|Title|Description|Priority|CreatedAt|Department|Assignee
 *   HW|UUID|Title|Description|Priority|CreatedAt|Department|Assignee|Serial|MakeModel|Location|FailureType|Warranty
 *
 *   [INCIDENTS]
 *   INCIDENT|IncidentTitle|IncidentDescription
 *   CHILD|ticketUUID
 *   CHILD|ticketUUID
 *   INCIDENT|AnotherTitle|AnotherDescription
 *   CHILD|ticketUUID
 */

public class StorageManager {
    private static final String FILE_NAME      = "ticket_storage.txt";
    private static final String SEC_TICKETS    = "[TICKETS]";
    private static final String SEC_INCIDENTS  = "[INCIDENTS]";
    private static final String PREFIX_INCIDENT = "INCIDENT|";
    private static final String PREFIX_CHILD    = "CHILD|";

    public StorageManager() {}
    
    // Methods
    public void saveAll(List<TicketComponentIF> tickets, List<IncidentComposite> incidents) {
        try (PrintWriter w = new PrintWriter(new FileWriter(FILE_NAME))) {
            //tickets
            w.println(SEC_TICKETS);
            for (TicketComponentIF t : tickets) {
                if (t.getTicketID() == null) continue;
                String dept = getDepartment(t);
                TicketComponentIF core = unwrapCore(t);
                if (core instanceof HardwareTicket hw) {
                    w.println(
                        "HW|" +
                        t.getTicketID()            + "|" +
                        escape(t.getTitle())       + "|" +
                        escape(t.getDescription()) + "|" +
                        t.getPriority()            + "|" +
                        t.getCreatedAt()           + "|" +
                        escape(dept)               + "|" +
                        escape(t.getAssignee())    + "|" +
                        escape(hw.getDeviceSerialNumber()) + "|" +
                        escape(hw.getDeviceMakeModel())    + "|" +
                        escape(hw.getOfficeLocation())     + "|" +
                        escape(hw.getFailureType())        + "|" +
                        hw.isUnderWarranty()
                    );
                } else {
                    w.println(
                        "SW|" +
                        t.getTicketID()            + "|" +
                        escape(t.getTitle())       + "|" +
                        escape(t.getDescription()) + "|" +
                        t.getPriority()            + "|" +
                        t.getCreatedAt()           + "|" +
                        escape(dept)               + "|" +
                        escape(t.getAssignee())
                    );
                }
            }
            w.println();

            //incidents
            w.println(SEC_INCIDENTS);
            for (IncidentComposite inc : incidents) {
                w.println(PREFIX_INCIDENT + escape(inc.getTitle()) + "|" + escape(inc.getDescription()));
                for (TicketComponentIF child : inc.getChildren()) {
                    if (child.getTicketID() != null) {
                        w.println(PREFIX_CHILD + child.getTicketID());
                    }
                }
            }
            System.out.println("StorageManager: Saved " + tickets.size() +
                               " tickets, " + incidents.size() + " incidents.");
        } catch (IOException e) {
            System.err.println("Save Error: " + e.getMessage());
        }
    }

    public static class LoadResult {
        public final List<TicketComponentIF>  tickets;
        public final List<IncidentComposite>  incidents;
        LoadResult(List<TicketComponentIF> t, List<IncidentComposite> i) {
            tickets = t; incidents = i;
        }
    }

    public LoadResult loadAll() {
        List<TicketComponentIF> tickets   = new ArrayList<>();
        List<IncidentComposite> incidents = new ArrayList<>();
        // UUID → ticket map for incident child look-up
        Map<UUID, TicketComponentIF> byId = new LinkedHashMap<>();

        File file = new File(FILE_NAME);
        if (!file.exists()) return new LoadResult(tickets, incidents);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String section = null;
            IncidentComposite currentIncident = null;
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) { currentIncident = null; continue; }

                if (line.equals(SEC_TICKETS)) {
                    section = "TICKETS"; currentIncident = null; continue;
                }
                if (line.equals(SEC_INCIDENTS)) {
                    section = "INCIDENTS"; currentIncident = null; continue;
                }

                if ("TICKETS".equals(section)) {
                    String type = "SW";
                    String parseLine = line;
                    if (line.startsWith("SW|") || line.startsWith("HW|")) {
                        type = line.substring(0, 2);
                        parseLine = line.substring(3);
                    }
                    String[] d = parseLine.split("\\|", -1);
                    if (d.length >= 5) {
                        try {
                            if ("HW".equals(type) && d.length >= 8) {
                                HardwareTicket hw = new HardwareTicket(
                                    UUID.fromString(d[0]),
                                    unescape(d[1]),
                                    unescape(d[2]),
                                    Integer.parseInt(d[3]),
                                    Instant.parse(d[4])
                                );
                                hw.setDepartment(unescape(d[5]));
                                hw.setAssignee(unescape(d[6]));
                                if (d.length >= 13) {
                                    hw.setDeviceSerialNumber(unescape(d[7]));
                                    hw.setDeviceMakeModel(unescape(d[8]));
                                    hw.setOfficeLocation(unescape(d[9]));
                                    hw.setFailureType(unescape(d[10]));
                                    hw.setUnderWarranty(Boolean.parseBoolean(d[11]));
                                }
                                tickets.add(hw);
                                byId.put(hw.getTicketID(), hw);
                            } else {
                                Ticket t = new Ticket(
                                    UUID.fromString(d[0]),
                                    unescape(d[1]),
                                    unescape(d[2]),
                                    Integer.parseInt(d[3]),
                                    Instant.parse(d[4])
                                );
                                if (d.length >= 7) {
                                    t.setDepartment(unescape(d[5]));
                                    t.setAssignee(unescape(d[6]));
                                }
                                tickets.add(t);
                                byId.put(t.getTicketID(), t);
                            }
                        } catch (Exception e) {
                            System.err.println("Skipping malformed ticket line: " + line);
                        }
                    }

                } else if ("INCIDENTS".equals(section)) {
                    if (line.startsWith(PREFIX_INCIDENT)) {
                        // INCIDENT|Title|Desc
                        String rest = line.substring(PREFIX_INCIDENT.length());
                        String[] parts = rest.split("\\|", 2);
                        String title = unescape(parts[0]);
                        String desc  = parts.length > 1 ? unescape(parts[1]) : "";
                        currentIncident = new IncidentComposite(title, desc);
                        incidents.add(currentIncident);

                    } else if (line.startsWith(PREFIX_CHILD) && currentIncident != null) {
                        String uuidStr = line.substring(PREFIX_CHILD.length()).trim();
                        try {
                            UUID id = UUID.fromString(uuidStr);
                            TicketComponentIF child = byId.get(id);
                            if (child != null) currentIncident.addChild(child);
                            else System.err.println("Warning: child UUID not found: " + id);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Skipping misformed child UUID: " + uuidStr);
                        }
                    }
                }
            }
            System.out.println("StorageManager: Loaded " + tickets.size() +
                               " tickets, " + incidents.size() + " incidents.");
        } catch (IOException e) {
            System.err.println("Load Error: " + e.getMessage());
        }
        return new LoadResult(tickets, incidents);
    }

    // Helpers

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n").replace("\r", "");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\");
    }

    private static String getDepartment(TicketComponentIF t) {
        TicketComponentIF core = unwrapCore(t);
        if (core instanceof Ticket) return ((Ticket) core).getDepartment();
        return "Unassigned";
    }

    private static TicketComponentIF unwrapCore(TicketComponentIF t) {
        TicketComponentIF cur = t;
        while (cur != null) {
            if (cur instanceof Ticket) return cur;
            try {
                java.lang.reflect.Field f = cur.getClass().getSuperclass().getDeclaredField("decoratedComponent");
                f.setAccessible(true);
                cur = (TicketComponentIF) f.get(cur);
            } catch (Exception e) {
                break;
            }
        }
        return t;
    }
}