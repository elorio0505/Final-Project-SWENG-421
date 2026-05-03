package edu.psu.processing;

import edu.psu.core.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TicketScheduler {
    private final PriorityQueue<TicketComponentIF> ticketQueue = new PriorityQueue<>(
            Comparator.comparing(TicketScheduler::getCreatedAt)
    );

    public interface DispatchListener {
        void onDispatched(TicketComponentIF ticket, SchedulerStats stats);
    }

    private final List<DispatchListener> listeners = new ArrayList<>();

    public void addDispatchListener(DispatchListener l) {
        listeners.add(l);
    }
    
    public static class SchedulerStats {
        public final int  pending;
        public final long totalSubmitted;
        public final long totalDispatched;
        public final long dispatchedThisSession;
        public final long elapsedMs;

        SchedulerStats(int pending, long totalSubmitted, long totalDispatched, long dispatchedThisSession, long elapsedMs) {
            this.pending = pending;
            this.totalSubmitted = totalSubmitted;
            this.totalDispatched = totalDispatched;
            this.dispatchedThisSession = dispatchedThisSession;
            this.elapsedMs = elapsedMs;
        }

        public double throughput() {
            if (elapsedMs <= 0) return 0;
            return dispatchedThisSession / (elapsedMs / 1000.0);
        }
    }

    private long totalSubmitted = 0;
    private long totalDispatched = 0;
    private long sessionDispatched = 0;
    private long sessionStartMs = 0;

    public synchronized SchedulerStats getStats() {
        long elapsed = sessionStartMs > 0 ? System.currentTimeMillis() - sessionStartMs : 0;
        return new SchedulerStats(ticketQueue.size(), totalSubmitted, totalDispatched, sessionDispatched, elapsed);
    }

    private Thread  workerThread = null;
    private boolean running = false;
    private boolean paused = false;

    private long intervalMs = 1000;

    // Constructor
    public TicketScheduler() {}
    public synchronized void start() {
        if (running) return;
        running          = true;
        paused           = false;
        sessionDispatched = 0;
        sessionStartMs   = System.currentTimeMillis();
        workerThread     = new Thread(this::workerLoop, "TicketScheduler-Worker");
        workerThread.setDaemon(true); // won't prevent JVM shutdown
        workerThread.start();
    }

    public synchronized void stop() {
        running = false;
        notifyAll();
        Thread t = workerThread;
        workerThread = null;
        if (t != null) {
            try { t.join(2000); } catch (InterruptedException ignored) {}
        }
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public synchronized boolean isRunning() { return running; }
    public synchronized boolean isPaused()  { return paused;  }

    public synchronized void setRate(double ticketsPerSecond) {
        double clamped = Math.max(0.1, Math.min(100.0, ticketsPerSecond));
        intervalMs = (long) (1000.0 / clamped);
        notifyAll();
    }

    public synchronized double getRate() {
        return 1000.0 / intervalMs;
    }

    public synchronized void submit(TicketComponentIF c) {
        if (c == null) throw new IllegalArgumentException("Cannot submit a null ticket.");
        ticketQueue.add(c);
        totalSubmitted++;
        notifyAll();
    }

    public synchronized int  getPendingCount()    { return ticketQueue.size(); }
    public synchronized boolean hasPendingTickets() { return !ticketQueue.isEmpty(); }

    private void workerLoop() {
        while (true) {
            TicketComponentIF ticket = null;
            long sleepMs;

            synchronized (this) { //scheduler synchronized
                if (!running) break;
                while (running && (paused || ticketQueue.isEmpty())) {
                    try { wait(); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (!running) break;

                ticket  = ticketQueue.poll();
                sleepMs = intervalMs;

                if (ticket != null) {
                    totalDispatched++;
                    sessionDispatched++;
                    IncidentRegistry.registerTicket(ticket);
                }
            }

            if (ticket != null) {
                SchedulerStats stats = getStats();
                for (DispatchListener l : listeners) {
                    l.onDispatched(ticket, stats);
                }
            }
            
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // Helper
    private static Instant getCreatedAt(TicketComponentIF t) {
        Instant ts = t.getCreatedAt();
        return ts != null ? ts : Instant.MAX;
    }
}
