package edu.psu.behavior;
import edu.psu.core.TicketComponentIF;

public abstract class AbsTicketState {
    public static final int ASSIGN_EVT = 1;
    public static final int ACTIVATE_EVT = 2;
    public static final int PENDING_EVT = 3;
    public static final int ESCALATE_EVT = 4;
    public static final int RESOLVE_EVT = 5;
    public static final int CLOSE_EVT = 6;
    public static final int REOPEN_EVT = 7;

    protected static final NewState newState = new NewState();
    protected static final AssignedState assignedState = new AssignedState();
    protected static final ActiveState activeState = new ActiveState();
    protected static final PendingState pendingState = new PendingState();
    protected static final EscalatedState escalatedState = new EscalatedState();
    protected static final ResolvedState resolvedState = new ResolvedState();
    protected static final ClosedState closedState = new ClosedState();

    public abstract void updateTicketStatus(TicketComponentIF context);
    
    public boolean validateTransition(int event) {
        return nextState(event) != null;
    }
    
    protected void enter(TicketComponentIF context) {};
    
    protected void exit(TicketComponentIF context) {};
    
    protected abstract AbsTicketState nextState(int event);
    
    public static AbsTicketState start() {
        return newState;
    }
    public void processEvent(TicketComponentIF context, int event) {
        if (validateTransition(event)) {
            AbsTicketState next = nextState(event);
            this.exit(context);
            context.setState(next);
            next.enter(context);
            next.updateTicketStatus(context);
        }
        else {
            System.out.println("Invalid transition for " + this.toString() + "with" + event);
        }
    }
    
    @Override
    public String toString() {
      return this.getClass().getSimpleName();  
    }
}
