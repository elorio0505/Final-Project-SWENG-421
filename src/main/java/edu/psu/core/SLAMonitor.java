package edu.psu.core;

// TODO: give a time to monitor when it's created, when checkSLAStatus() is called, return true if the current time is before t

public class SLAMonitor extends AbsTicketDecorator {

    public SLAMonitor(TicketComponentIF c) {

        super(c);
    }

    public boolean checkSLAStatus() {return false;}

}
