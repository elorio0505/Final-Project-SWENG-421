package edu.psu.core;

public class SLAMonitor extends AbsTicketDecorator {

    public SLAMonitor(TicketComponentIF c) {
        super(c);
    }

    public boolean checkSLAStatus() {return false;}

}
