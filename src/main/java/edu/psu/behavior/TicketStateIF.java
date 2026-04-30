package edu.psu.behavior;
// TODO: Please implement a toString method for the sake of printing, thank you
public interface TicketStateIF {
    int ASSIGN_EVT = 1;
    int ACTIVATE_EVT = 2;
    int PENDING_EVT = 3;
    int ESCALATE_EVT = 4;
    int RESOLVE_EVT = 5;
    int CLOSE_EVT = 6;
    int REOPEN_EVT = 7;

    // The UML had these fields and some methods as protected, but you can't do that in interfaces; change to whatever is needed
    NewState newState = new NewState();
    AssignedState assignedState = new AssignedState();
    ActiveState activeState = new ActiveState();
    PendingState pendingState = new PendingState();
    EscalatedState escalatedState = new EscalatedState();
    ResolvedState resolvedState = new ResolvedState();
    ClosedState closedState = new ClosedState();

    void updateTicketStatus();
    boolean validateTransition();
    void enter();
    void exit();
    TicketStateIF nextState();
    TicketStateIF start();
    TicketStateIF processEvent(int event);
}
