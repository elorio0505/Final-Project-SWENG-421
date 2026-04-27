package edu.psu.behavior;

public interface TicketStateIF {
    public final int ASSIGN_EVT = 1;
    public final int ACTIVATE_EVT = 2;
    public final int PENDING_EVT = 3;
    public final int ESCALATE_EVT = 4;
    public final int RESOLVE_EVT = 5;
    public final int CLOSE_EVT = 6;
    public final int REOPEN_EVT = 7;

    // The UML had these fields and some methods as protected, but you can't do that in interfaces; change to whatever is needed
    public NewState newState = new NewState();
    public AssignedState assignedState = new AssignedState();
    public ActiveState activeState = new ActiveState();
    public PendingState pendingState = new PendingState();
    public EscalatedState escalatedState = new EscalatedState();
    public ResolvedState resolvedState = new ResolvedState();
    public ClosedState closedState = new ClosedState();

    public void updateTicketStatus();
    public boolean validateTransition();
    public void enter();
    public void exit();
    public TicketStateIF nextState();
    public TicketStateIF start();
    public TicketStateIF processEvent(int event);

}
