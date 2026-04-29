package edu.psu.core;

// TODO: Have a log that is made on instantiation and when logChange is called, append to it

public class AuditLogger extends AbsTicketDecorator {

    public AuditLogger(TicketComponentIF c) {
        super(c);
    }

    public void logChange(String log) {}

}
