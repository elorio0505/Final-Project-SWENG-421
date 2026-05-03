package edu.psu.behavior;
import edu.psu.core.TicketComponentIF;

public class ResolvedState extends AbsTicketState {
    @Override
    public void updateTicketStatus(TicketComponentIF context) {
        System.out.println("Ticket" + context.getTicketID() + "is resolved");
    }

    @Override
    protected AbsTicketState nextState(int event) {
        if (event == CLOSE_EVT) return closedState;
        if (event == REOPEN_EVT) return activeState;
        return null;
    }
    @Override
    public String toString() {
        return "Resolved State";
    }
}
