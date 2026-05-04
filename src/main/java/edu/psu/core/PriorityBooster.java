package edu.psu.core;

public class PriorityBooster extends AbsTicketDecorator {

    public PriorityBooster(TicketComponentIF c) {
        super(c);
    }

    /**
     * Boosts the priority of wrapped component by 1
     */
    public void boost() {
        setPriority(getPriority() + 1);
    }
}
