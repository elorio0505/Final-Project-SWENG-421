package edu.psu.processing;

import edu.psu.core.TicketComponentIF;

import java.time.Instant;

public class HardwareTicketBuilder extends AbsTicketBuilder {
    private String deviceSerialNumber;
    private String officeLocation;

    public HardwareTicketBuilder() {}

    @Override
    public void setBasics(String title, String desc) {
        this.title = title;
        this.description = desc;
    }

    @Override
    public void setMetaData(String source, int priority) {
        this.source = source;
        this.priority = priority;
        this.createdAt = Instant.now();
    }

    public void setHardwareDetails(String serial, String location) {
        this.deviceSerialNumber = serial;
        this.officeLocation = location;
    }

    @Override
    public void reset() {
        super.reset();
        deviceSerialNumber = null;
        officeLocation = null;
    }

    @Override
    public TicketComponentIF getProduct() {return null;}
}
