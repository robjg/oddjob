package org.oddjob.beanbus.pipeline;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

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

    public void block() {

        sync.block();
    }

    public void unblock() {

        sync.unblock();
    }

    public void await() throws InterruptedException {

        sync.acquireSharedInterruptibly(1);
    }

    public void await(long millis) throws InterruptedException, TimeoutException {

        if (!sync.tryAcquireSharedNanos( 1,millis * 1000L))
            throw new TimeoutException();
    }

    public int getBlockers() {
        return sync.getBlockers();
    }

    public int getBlocked() {
        return sync.getQueueLength();
    }
}
