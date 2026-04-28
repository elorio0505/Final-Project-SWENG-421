package edu.psu.processing;

import edu.psu.core.*;
import java.util.LinkedList;
import java.util.Queue;

public class TicketScheduler {
    private final Queue<TicketComponentIF> ticketQueue = new LinkedList<>();
    private boolean busy = false;

    public TicketScheduler() {}

    /**
     * Submit ticket into scheduler.
     */
    public synchronized void submit(TicketComponentIF c) throws InterruptedException {
        ticketQueue.add(c);
        while (busy || ticketQueue.peek() != c) {
            wait();
        }
        busy = true;
    }

    /**
     * Called by the running thread when done.
     */
    public synchronized TicketComponentIF dispatchNext() {
        if (ticketQueue.isEmpty()) {
            throw new IllegalStateException("No ticket is currently running");
        }
        TicketComponentIF finished = ticketQueue.remove();
        busy = false;
        notifyAll();
        return finished;
    }
}