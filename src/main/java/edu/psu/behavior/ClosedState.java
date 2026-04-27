package edu.psu.behavior;

public class ClosedState implements TicketStateIF {
    @Override
    public void updateTicketStatus() {

    }

    @Override
    public boolean validateTransition() {
        return false;
    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public TicketStateIF nextState() {
        return null;
    }

    @Override
    public TicketStateIF start() {
        return null;
    }

    @Override
    public TicketStateIF processEvent(int event) {
        return null;
    }
}
