package org.oddjob.state;

import org.oddjob.Stateful;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Wait for a job to start Executing. This is a helper class for Structural
 * Jobs to ensure that the child job has moved from the ready state before it
 * starts reflecting child states. Without the wait it's very difficult to
 * check tests because a late starting asynchronous job would cause this
 * job to have states READY-EXECUTING-READY-ACTIVE as opposed to
 * READY-EXECUTING-ACTIVE which is what is expected.
 *
 * @author rob
 */
public class AsyncJobWait implements Serializable {
    private static final long serialVersionUID = 2015041600L;

    private static final Logger logger =
            LoggerFactory.getLogger(AsyncJobWait.class);

    private volatile transient Thread thread;

    private volatile transient boolean join;

    public void setJoin(boolean join) {
        this.join = join;
    }

    public boolean isJoin() {
        return join;
    }

    /**
     * Run the job watching state
     *
     * @param job The job to wait for to start executing.
     * @return Will return true if the job is asynchronous.
     */
    public boolean runAndWaitWith(Runnable job) {

        final BlockingQueue<State> states = new LinkedBlockingQueue<>();
        StateListener listener = event -> states.add(event.getState());

        ((Stateful) job).addStateListener(listener);

        try {

            job.run();

            if (isJoin()) {
                return new JoinStrategy(states).doWait();
            } else {
                return new NonJoinStrategy(states).doWait();
            }
        } finally {
            ((Stateful) job).removeStateListener(listener);

            if (Thread.interrupted()) {
                logger.info("Thread was interrupted");
            }
        }
    }

    interface WaitStrategy {
        boolean doWait();
    }

    class NonJoinStrategy implements WaitStrategy {

        final BlockingQueue<State> states;

        public NonJoinStrategy(BlockingQueue<State> states) {
            this.states = states;
        }

        @Override
        public boolean doWait() {
            State now = states.remove();

            // Was probably not reset. Pretend it's asynchronous.
            if (!now.isReady()) {
                return true;
            }

            thread = Thread.currentThread();
            try {
                now = states.take();
            } catch (InterruptedException e) {
                // An interrupted wait indicates it was asynchronous.
                return true;
            } finally {
                thread = null;
            }

            logger.debug("NoneJoin, State received {}", now);

            if (now.isDestroyed()) {
                childDestroyed();
            }

            while (true) {
                now = states.poll();
                // Still Executing
                if (now == null) {
                    return true;
                }
                if (StateConditions.LIVE.test(now)) {
                    return true;
                }
                if (now.isComplete()) {
                    return false;
                }
            }
        }
    }


    class JoinStrategy implements WaitStrategy {

        final BlockingQueue<State> states;

        public JoinStrategy(BlockingQueue<State> states) {
            this.states = states;
        }

        @Override
        public boolean doWait() {

            while (true) {
                State now;
                thread = Thread.currentThread();
                try {
                    now = states.take();
                } catch (InterruptedException e) {
                    return false;
                } finally {
                    thread = null;
                }

                logger.debug("Join, State received {}", now);

                if (now.isDestroyed()) {
                    childDestroyed();
                }

                if (StateConditions.FINISHED.test(now)) {
                    return false;
                }
            }
        }
    }

    protected void childDestroyed() {
        throw new IllegalStateException("Job Destroyed.");
    }

    public void stopWait() {
        Thread thread = this.thread;
        if (thread != null) {
            thread.interrupt();
        }
    }
}
