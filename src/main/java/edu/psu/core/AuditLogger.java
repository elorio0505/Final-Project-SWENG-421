package edu.psu.core;

import edu.psu.behavior.AbsTicketState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AuditLogger extends AbsTicketDecorator {
    private final List<String> logs ;

    // Constructor
    public AuditLogger(TicketComponentIF c) {
        super(c);
        logs = new ArrayList<>();
        logChange("AuditLogger initialized");
    }

    // Methods
    /**
     * Adds a formatted log entry to the logs
     * @param logMessage Textual description of the log entry
     */
    public void logChange(String logMessage) {
        logs.add(getTimestamp() + " | " + logMessage);
    }

    // Getters
    public List<String> getLogs() {
        return logs;
    }

    /**
     * Get current time
     * @return current time as a String
     */
    public String getTimestamp() {
        return Instant.now().toString();
    }

    @Override
    public void processEvent(int event) {
        logChange("Event " + event + " executed");
        super.processEvent(event);
    }

    @Override
    public void setPriority(int priority) {
        logChange("Set priority to " + priority);
        super.setPriority(priority);
    }

    @Override
    public void setState(AbsTicketState state) {
        logChange("State changed to " + state);
        super.setState(state);
    }
}
