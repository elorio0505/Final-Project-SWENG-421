package edu.psu.processing;

import edu.psu.core.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.PriorityQueue;

public class TicketScheduler {
    private final PriorityQueue<TicketComponentIF> ticketQueue = new PriorityQueue<>(
            Comparator.comparing(TicketScheduler::getTicketCreatedAt)
    );

    public TicketScheduler() {}

    /**
     * Submit ticket into scheduler in timestamp order.
     */
    public synchronized void submit(TicketComponentIF c) {
        if (c == null) {
            throw new IllegalArgumentException("Cannot submit a null ticket.");
        }

        ticketQueue.add(c);
        notifyAll();
    }

    /**
     * Dispatches the oldest submitted ticket.
     */
    public synchronized TicketComponentIF dispatchNext() {
        if (ticketQueue.isEmpty()) {
            throw new IllegalStateException("No tickets are waiting to be dispatched.");
        }

        return ticketQueue.remove();
    }

    public synchronized int getPendingCount() {
        return ticketQueue.size();
    }

    public synchronized boolean hasPendingTickets() {
        return !ticketQueue.isEmpty();
    }

    private static Instant getTicketCreatedAt(TicketComponentIF ticket) {
        Instant createdAt = ticket.getCreatedAt();
        return createdAt == null ? Instant.MAX : createdAt;
    }
}