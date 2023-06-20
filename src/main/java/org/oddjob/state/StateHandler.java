package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.util.OddjobLockedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;


/**
 * Helps Jobs handle state change.
 * <p>
 * The state setting operations don't notify the listeners. This must be done using the separate
 * {@link #fireEvent()} method. This is because jobs that use this need to persist their
 * themselves after setting the state.
 * </p>
 * <p>
 * Lock must be held during setting and firing operations. Considered using
 * a read/write lock so state could be read while event fired to listeners,
 * however as read write locks are not as performant as a single mutex lock,
 * it was deemed not worth it.
 * </p>
 * <p>
 * Todo:
 *     <ul>
 *         <li>Attempted to make {@link #waitToWhen(StateCondition, Runnable)} and
 *         {@link #tryToWhen(StateCondition, Runnable) both use timeouts. This
 *         now required interrupt handling and the tryLock was intermittently
 *         interrupted. The cause of this could not be found so attempting to
 *         implement timeouts was abandoned for the time being.</li>
 *     </ul>
 * </p>
 *
 * @author Rob Gordon
 */

public class StateHandler<S extends State>
        implements Stateful, StateLock {

    private static final Logger logger = LoggerFactory.getLogger(StateHandler.class);

    private final S readyState;

    /**
     * The source.
     */
    private final Stateful source;

    /**
     * State listeners
     */
    private final List<StateListener> listeners =
            new CopyOnWriteArrayList<>();

    /**
     * The last event
     */
    private volatile StateEvent lastEvent;

    /**
     * Used to stop listeners changing state.
     */
    private boolean firing;

    /**
     * Used for the state lock.
     */
    private final ReentrantLock lock = new ReentrantLock(true) {
        private static final long serialVersionUID = 2010080400L;

        public String toString() {
            Thread o = getOwner();
            return "[" + source + "]" +
                    ((o == null) ?
                            "[Unlocked]" :
                            "[Locked by thread " + o.getName() + "]");
        }
    };

    private final Condition alarm = lock.newCondition();

    /**
     * Constructor.
     *
     * @param source     The source for events.
     * @param readyState The ready state.
     */
    public StateHandler(Stateful source, S readyState) {
        this.source = source;
        lastEvent = StateEvent.now(source, readyState);
        this.readyState = readyState;
    }

    /**
     * Get the last event.
     *
     * @return The last event.
     */
    @Override
    public StateEvent lastStateEvent() {
        return lastEvent;
    }

    /**
     * Typically only called after restoring a jobstate handler after deserialisation.
     *
     * @param savedEvent The last serialised event.
     */
    public void restoreLastJobStateEvent(StateDetail savedEvent) {

        // If state was saved when executing it now has to
        // be ready, because oddjob must have crashed last time.
        if (savedEvent.getState().isStoppable()) {
            lastEvent = StateEvent.now(source, readyState);
        } else {
            lastEvent = savedEvent.toEvent(source);
        }
    }

    /**
     * Deprecated - use {@link #setState(State, StateInstant)}.
     *
     */
    @Deprecated(since="1.7", forRemoval=true)
    public void setState(S state, Date date) throws JobDestroyedException {
        setLastJobStateEvent(StateEvent.atInstant(source, state, StateInstant.forOneVersionOnly(date.toInstant())));
    }

    /**
     * Set the state.
     * 
     * @param state The state.
     * @param instant The time instant for the event.
     * 
     * @throws JobDestroyedException If the current state is DESTROYED.
     *
     * @see org.oddjob.state.StateChanger#setState(State, StateInstant).
     */
    public void setState(S state, StateInstant instant) throws JobDestroyedException {
        setLastJobStateEvent(StateEvent.atInstant(source, state, instant));
    }

    /**
     * Set the state.
     *
     * @param state The state.
     *
     * @throws JobDestroyedException If the current state is DESTROYED.
     *
     * @see org.oddjob.state.StateChanger#setStateException(Throwable).
     */
    public void setState(S state) throws JobDestroyedException {
        setLastJobStateEvent(StateEvent.now(source, state));
    }

    /**
     * Set the Exception state and time instant of the event.
     *
     * @param state The exception state
     * @param t The exception.
     * @param instant The time instant for the event.
     *
     * @throws JobDestroyedException If the current state is DESTROYED.
     *
     * @see org.oddjob.state.StateChanger#setStateException(Throwable, StateInstant).
     */
    public void setStateException(S state, Throwable t, StateInstant instant) throws JobDestroyedException {
        Objects.requireNonNull(state);
        Objects.requireNonNull(t);
        Objects.requireNonNull(instant);

        setLastJobStateEvent(
                StateEvent.exceptionAtInstant(source, state, instant, t));
    }

    /**
     * Deprecated - use {@link #setStateException(State, Throwable, StateInstant)}.
     *
     */
//    @Deprecated(since="1.7", forRemoval=true)
//    public void setStateException(S state, Throwable t, Date date) throws JobDestroyedException {
//        setLastJobStateEvent(
//                new StateEvent(source, state, date, t));
//    }

    /**
     * Set the exception state.
     *
     * @param state The exception state.
     * @param ex The exception.
     *
     * @throws JobDestroyedException If the existing state is DESTROYED.
     *
     * @see StateChanger#setStateException(Throwable)
     */
    public void setStateException(State state, Throwable ex) throws JobDestroyedException {
        setLastJobStateEvent(StateEvent.exceptionNow(source, state, ex));
    }

    private void setLastJobStateEvent(StateEvent event) throws JobDestroyedException {
        assertAlive();
        assertLockHeld();

        if (firing) {
            throw new IllegalStateException(
                    "Can't change state from a listener!");
        }

        lastEvent = event;
    }

    /**
     * Return the current state of the job.
     *
     * @return The current state.
     */
    public State getState() {
        return lastEvent.getState();
    }

    /**
     * Convenience method to check the job hasn't been destroyed.
     *
     * @throws JobDestroyedException If it has.
     */
    public void assertAlive() throws JobDestroyedException {
        if (lastEvent.getState().isDestroyed()) {
            throw new JobDestroyedException(source);
        }
    }

    public void assertLockHeld() {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("[" + source + "] State Lock not held by thread [" +
                    Thread.currentThread().getName() + "]");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.state.StateLock#tryToWhen(org.oddjob.state.StateCondition, java.lang.Runnable)
     */
    public boolean tryToWhen(StateCondition when, Runnable runnable)
            throws OddjobLockedException {

        if (!lock.tryLock()) {
            throw new OddjobLockedException(lock.toString());
        }
        try {
            return doWhen(when, runnable);
        } finally {
            lock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.oddjob.state.StateLock#waitToWhen(org.oddjob.state.StateCondition, java.lang.Runnable)
     */
    public boolean waitToWhen(StateCondition when, Runnable runnable) {
        lock.lock();
        try {
            return doWhen(when, runnable);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Do the work that will be executed when this thread holds
     * the lock.
     *
     * @param when     Only do something if this condition is met.
     * @param runnable The thing to do while holding the lock.
     * @return true if the test is true and the work is done, false
     * otherwise.
     */
    private boolean doWhen(StateCondition when, Runnable runnable) {
        if (when.test(lastEvent.getState())) {
            runnable.run();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Wait to acquire the lock and execute the Runnable while holding the lock.
     *
     * @param runnable The Runnable.
     */
    public void runLocked(Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Wait to acquire the lock and execute the Callable while holding the lock.
     *
     * @param callable The callable.
     * @return The result of the callable.
     * @throws Exception from the callable.
     */
    public <T> T callLocked(Callable<T> callable) throws Exception {
        lock.lock();
        try {
            return callable.call();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Wait to acquire the lock and execute the Supplier while holding the lock.
     *
     * @param supplier The Supplier.
     * @return The result from the Supplier.
     */
    public <T> T supplyLocked(Supplier<T> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Sleep.
     *
     * @param time The milliseconds to sleep for.
     * @throws InterruptedException If the sleep is interrupted.
     */
    public void sleep(long time) throws InterruptedException {
        assertLockHeld();

        if (time == 0) {
            alarm.await();
        } else {
            alarm.await(time, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Wake any threads that are sleeping via {@link #sleep(long)}.
     */
    public void wake() {
        assertLockHeld();

        alarm.signalAll();
    }

    /**
     * Add a job state listener. This method will send the last event
     * to the new listener. It is possible that the listener may get the
     * notification twice.
     *
     * @param listener The listener.
     * @throws JobDestroyedException If trying to listen to a destroyed job.
     */
    public void addStateListener(final StateListener listener)
            throws JobDestroyedException {
        assertAlive();

        runLocked(() -> {
            listeners.add(listener);
            // setting firing flag stops the listener chaining state.
            firing = true;
            try {
                listener.jobStateChange(lastEvent);
            } finally {
                firing = false;
            }
        });
    }


    /**
     * Remove a job state listener.
     *
     * @param listener The listener.
     */
    public void removeStateListener(final StateListener listener) {
        runLocked(() -> listeners.remove(listener));
    }

    /**
     * The number of listeners.
     *
     * @return The number of listeners.
     */
    public int listenerCount() {
        return listeners.size();
    }

    /**
     * Override toString.
     */
    public String toString() {
        return "JobStateHandler( " + lastEvent.getState() + " )";
    }

    /**
     * Fire the event, update last event.
     */
    public void fireEvent() {
        assertLockHeld();

        if (firing) {
            throw new IllegalStateException(
                    "Can't fire event from a listener!");
        }

        firing = true;
        try {
            doFireEvent(lastEvent);
        } finally {
            firing = false;
        }
    }

    private void doFireEvent(StateEvent event) {
        if (event == null) {
            throw new NullPointerException("No JobStateEvent.");
        }

        for (StateListener listener : listeners) {
            try {
                listener.jobStateChange(event);
            } catch (Throwable t) {
                logger.error("Failed notifying listener [" + listener
                        + "] of event [" + event + "]", t);
            }
        }
    }

}

