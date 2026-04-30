package edu.psu;

import edu.psu.core.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {
        Ticket t1 = new Ticket(UUID.randomUUID(), "Test Ticket 1", 1);
        Ticket t2 = new Ticket(UUID.randomUUID(), "Test Ticket 2", 2);
        Ticket t3 = new Ticket(UUID.randomUUID(), "Test Ticket 3", 3);
        Ticket t4 = new Ticket(UUID.randomUUID(), "Test Ticket 3", 4);

        IncidentComposite i1 = IncidentRegistry.newIncident("Test Incident 1", "This is a test incident.");
        i1.add(t1);
        i1.add(t2);
        i1.add(t3);
        i1.add(t4);

        AuditLogger auditLogger = new AuditLogger(t2);
        for (String log : auditLogger.getLogs()) {
            System.out.println(log);
        }

        SLAMonitor slaMonitor = new SLAMonitor(t3, Duration.ofHours(3));
        System.out.println(slaMonitor.checkSLAStatus());

        PriorityBooster priorityBooster = new PriorityBooster(t4);
        priorityBooster.boost();
        System.out.println(priorityBooster.getPriority());

        i1.remove(t1);
    }
}
