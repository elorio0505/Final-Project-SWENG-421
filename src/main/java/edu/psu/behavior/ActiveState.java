package edu.psu.behavior;
import edu.psu.core.TicketComponentIF;
        
public class ActiveState extends AbsTicketState {
    @Override
    public void updateTicketStatus(TicketComponentIF context) {
        System.out.println("Ticket" + context.getTicketID() + " is active");
    }

    @Override
    protected AbsTicketState nextState(int event) {
        switch (event) {
            case PENDING_EVT: return pendingState;
            case ESCALATE_EVT: return escalatedState;
            case RESOLVE_EVT: return resolvedState;
            default: return null;
        }
    }
    @Override
    public String toString() {
        return "Active State";
    }
}
