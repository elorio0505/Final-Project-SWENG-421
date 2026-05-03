package edu.psu.core;

// TODO: give a time to monitor when it's created, when checkSLAStatus() is called, return true if the current time is before t

import java.time.Duration;
import java.time.Instant;

public class SLAMonitor extends AbsTicketDecorator {

    private final Instant createdAt;
    private final Duration slaDuration;

    /**
     * When created, captures current time
     * @param c wrapped TicketComponent
     * @param slaDuration length of time before SLA deadline
     */
    public SLAMonitor(TicketComponentIF c, Duration slaDuration) {
        super(c);
        this.createdAt = Instant.now();
        this.slaDuration = slaDuration;
    }

    /**
     * Returns whether the SLA deadline has been met or not
     * @return truthiness of SLA status
     */
    public boolean checkSLAStatus() {
        Instant now = Instant.now();
        Instant deadline = createdAt.plus(slaDuration);
        return now.isBefore(deadline);
    }

}