package edu.psu.core;

public class AuditLogger extends AbsTicketDecorator {

    public AuditLogger(TicketComponentIF c) {
        super(c);
    }

    public void logChange(String log) {}

}
