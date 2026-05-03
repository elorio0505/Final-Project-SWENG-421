package edu.psu.processing;

import edu.psu.core.*;
import java.util.UUID;
import java.time.Instant;

public class HardwareTicketBuilder extends AbsTicketBuilder {
    private String deviceSerialNumber = "";
    private String deviceMakeModel    = "";
    private String officeLocation     = "";
    private String failureType        = "";
    private boolean underWarranty     = false;

    public HardwareTicketBuilder() {}

    @Override
    public void setBasics(String title, String desc) {
        this.title       = title;
        this.description = desc;
    }

    @Override
    public void setMetaData(String source, int priority) {
        this.source    = source;
        this.priority  = priority;
        this.createdAt = Instant.now();
    }

    public void setHardwareDetails(String serial, String makeModel,
                                   String location, String failureType,
                                   boolean underWarranty) {
        this.deviceSerialNumber = serial      == null ? "" : serial;
        this.deviceMakeModel    = makeModel   == null ? "" : makeModel;
        this.officeLocation     = location    == null ? "" : location;
        this.failureType        = failureType == null ? "" : failureType;
        this.underWarranty      = underWarranty;
    }

    @Override
    public void reset() {
        super.reset();
        deviceSerialNumber = "";
        deviceMakeModel    = "";
        officeLocation     = "";
        failureType        = "";
        underWarranty      = false;
    }

    @Override
    public TicketComponentIF getProduct() {
        if (this.createdAt == null) this.createdAt = Instant.now();
        HardwareTicket ticket = new HardwareTicket(UUID.randomUUID(), title, description, priority, createdAt);
        ticket.setDeviceSerialNumber(deviceSerialNumber);
        ticket.setDeviceMakeModel(deviceMakeModel);
        ticket.setOfficeLocation(officeLocation);
        ticket.setFailureType(failureType);
        ticket.setUnderWarranty(underWarranty);
        IncidentRegistry.registerTicket(ticket);
        reset();
        return ticket;
    }
}