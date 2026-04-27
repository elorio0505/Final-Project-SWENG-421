package edu.psu.core;

public class PriorityBooster extends AbsTicketDecorator {

    public PriorityBooster(TicketComponentIF c) {
        super(c);
    }

    public void boost() {}
}
