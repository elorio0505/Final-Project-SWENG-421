package edu.psu.processing;

import edu.psu.core.*;
import java.util.PriorityQueue;

public class TicketScheduler {
    private PriorityQueue<TicketComponentIF> ticketQueue;

    public TicketScheduler() {};

    public void submit(TicketComponentIF c) {}
    public TicketComponentIF dispatchNext() {return null;}
}
