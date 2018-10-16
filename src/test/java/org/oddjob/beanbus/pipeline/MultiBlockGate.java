package org.oddjob.beanbus.pipeline;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * A gate that can be blocked by any number independent parties and will remain blocked until unblocked by the same
 * number of independent parties. The gate is reusable and be blocked an unblocked continually.
 */
public class MultiBlockGate {

    private static class Sync extends AbstractQueuedSynchronizer {

        protected int tryAcquireShared(int ignore) {

            return getState() == 0 ? 1 : -1;
        }

        protected boolean tryReleaseShared(int ignore) {
            return getState() == 0;
        }

        protected void block() {
            for (int now = getState(); !compareAndSetState( now, now + 1); now = getState());
        }

        protected void unblock() {
            for (int now = getState(); !compareAndSetState( now, now - 1); now = getState());
            releaseShared(1);
        }

        int getBlockers() {
            return getState();
        }
    }

    private final Sync sync = new Sync();

    /**
     * Block the gate.
     */
    public void block() {

        sync.block();
    }

    /**
     * Unblock the gate.
     */
    public void unblock() {

        sync.unblock();
    }

    /**
     * Wait indefinitely until the gate is unblocked.
     *
     * @throws InterruptedException If the wait is interrupted.
     */
    public void await() throws InterruptedException {

        sync.acquireSharedInterruptibly(1);
    }

    /**
     * Wait the given number of milliseconds until the gate is unblocked.
     *
     * @param millis The number of milliseconds to wait for.
     *
     * @throws InterruptedException If the wait is interrupted.
     * @throws TimeoutException If the gate is unblocked in the given time.
     */
    public void await(long millis) throws InterruptedException, TimeoutException {

        if (!sync.tryAcquireSharedNanos( 1,millis * 1000L))
            throw new TimeoutException();
    }

    /**
     * Get the number of blocks on the gate.
     *
     * @return The number of blocks or 0 if the gate is not blocked.
     */
    public int getBlockers() {
        return sync.getBlockers();
    }

    /**
     * Get the number of threads blocked by the gate.
     *
     * @return The number of thread blocked.
     */
    public int getBlocked() {
        return sync.getQueueLength();
    }
}
