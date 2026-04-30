package edu.psu.core;

// TODO: Maybe more "levels" of priority boosting? This is pretty barebones as it stands.

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
