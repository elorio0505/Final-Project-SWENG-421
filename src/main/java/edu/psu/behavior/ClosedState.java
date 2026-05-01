package edu.psu.behavior;
import edu.psu.core.TicketComponentIF;

public class ClosedState extends AbsTicketState {
        @Override
    public void updateTicketStatus(TicketComponentIF context) {
        System.out.println("Ticket" + context.getTicketID() + " is closed");
    }

    @Override
    protected AbsTicketState nextState(int event) {
        if (event == REOPEN_EVT) return activeState;
        return null;
    }
    
    @Override
    public String toString() {
        return "closed State";
    }
}
