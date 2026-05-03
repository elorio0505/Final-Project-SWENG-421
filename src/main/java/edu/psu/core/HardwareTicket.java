package edu.psu.core;

import edu.psu.behavior.AbsTicketState;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * A hardware-specific ticket that extends Ticket with fields for device
 * serial number, make/model, office location, warranty status, and failure type.
 */
public class HardwareTicket extends Ticket {

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private String deviceSerialNumber = "N/A";
    private String deviceMakeModel    = "N/A";
    private String officeLocation     = "N/A";
    private String failureType        = "N/A";
    private boolean underWarranty     = false;

    // Used by builder (new tickets)
    public HardwareTicket(UUID id, String title, String description, int priority) {
        super(id, title, description, priority);
    }

    // Used by StorageManager (loaded tickets)
    public HardwareTicket(UUID id, String title, String description, int priority, Instant createdAt) {
        super(id, title, description, priority, createdAt);
    }

    // ── Hardware setters ──────────────────────────────────────────────────────
    public void setDeviceSerialNumber(String s)  { this.deviceSerialNumber = s == null ? "N/A" : s; }
    public void setDeviceMakeModel(String s)     { this.deviceMakeModel    = s == null ? "N/A" : s; }
    public void setOfficeLocation(String s)      { this.officeLocation     = s == null ? "N/A" : s; }
    public void setFailureType(String s)         { this.failureType        = s == null ? "N/A" : s; }
    public void setUnderWarranty(boolean w)      { this.underWarranty = w; }

    // ── Hardware getters ──────────────────────────────────────────────────────
    public String getDeviceSerialNumber() { return deviceSerialNumber; }
    public String getDeviceMakeModel()    { return deviceMakeModel; }
    public String getOfficeLocation()     { return officeLocation; }
    public String getFailureType()        { return failureType; }
    public boolean isUnderWarranty()      { return underWarranty; }

    @Override
    public String displayDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID:           ").append(getTicketID()).append("\n");
        sb.append("Type:           Hardware\n");
        sb.append("Title:          ").append(getTitle()).append("\n");
        sb.append("Created:        ").append(FMT.format(getCreatedAt())).append("\n");
        sb.append("Status:         ").append(getState()).append("\n");
        sb.append("Priority:       ").append(getPriority()).append("\n");
        sb.append("Department:     ").append(getDepartment()).append("\n");
        sb.append("Assignee:       ").append(getAssignee()).append("\n");
        sb.append("Description:    ").append(getDescription()).append("\n");
        sb.append("\n── Hardware Details ─────────────────────\n");
        sb.append("Serial Number:  ").append(deviceSerialNumber).append("\n");
        sb.append("Make / Model:   ").append(deviceMakeModel).append("\n");
        sb.append("Location:       ").append(officeLocation).append("\n");
        sb.append("Failure Type:   ").append(failureType).append("\n");
        sb.append("Under Warranty: ").append(underWarranty ? "Yes" : "No").append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "[HW][" + getState() + "] " + getTitle();
    }
}