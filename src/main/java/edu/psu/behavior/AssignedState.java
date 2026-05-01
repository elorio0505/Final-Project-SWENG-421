package edu.psu.behavior;
import edu.psu.core.TicketComponentIF;
        
public class AssignedState extends AbsTicketState {
    @Override
    public void updateTicketStatus(TicketComponentIF context) {
        System.out.println("Ticket" + context.getTicketID() + " assigned to " + context);
    }

    @Override
    protected AbsTicketState nextState(int event) {
        if (event == ACTIVATE_EVT) return activeState;
        return null;
    }
    @Override
    public String toString() {
        return "Assigned State";
    }
}
