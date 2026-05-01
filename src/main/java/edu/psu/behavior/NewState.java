package edu.psu.behavior;
import edu.psu.core.TicketComponentIF;

public class NewState extends AbsTicketState {
    @Override
    public void updateTicketStatus(TicketComponentIF context) {
        System.out.println("Ticket" + context.getTicketID() + " awaiting assignment");
    }

    @Override
    protected AbsTicketState nextState(int event) {
        if (event == ASSIGN_EVT) return assignedState;
        return null;
    }
    @Override
    public String toString() {
        return "New State";
    }
}
