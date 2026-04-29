package edu.psu.core;

// TODO: increment priority of component when boost() called

public class PriorityBooster extends AbsTicketDecorator {
    public PriorityBooster(TicketComponentIF c) {
        super(c);
    }

    public void boost() {}
}
