package org.oddjob.state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.util.OddjobLockedException;


/**
 * Helps Jobs handle state change.
 * 
 * @author Rob Gordon
 */

public class StateHandler<S extends State> 
implements Stateful, StateLock {
	
	private static final Logger logger = Logger.getLogger(StateHandler.class);
	
	private final S readyState;
	
	/** The source. */
	private final Stateful source;
	
	/** State listeners */
	private transient ArrayList<StateListener> listeners = 
		new ArrayList<StateListener>();

	/** The last event */
	private volatile StateEvent lastEvent;

	/** Used to stop listeners changing state. */
	private transient boolean fireing; 
	
	/** Used for the state lock. */
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
	 * Constrctor.
	 * 
	 * @param source The source for events.
	 */
	public StateHandler(Stateful source, S readyState) {
		this.source = source;
		lastEvent = new StateEvent(source, readyState, null);
		this.readyState = readyState;
	}	
	
	/**
	 * Get the last event.
	 * 
	 * @return The last event.
	 */
	@Override
	public StateEvent lastStateEvent() {
		final AtomicReference<StateEvent> result = 
			new AtomicReference<StateEvent>();
		waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				result.set(lastEvent);
			}
		});
		return result.get();	
	}
	
	/**
	 * Typically only called after restoring a jobstate handler after deserialisation.
	 * 
	 * @param source
	 */
	public void restoreLastJobStateEvent(StateEvent savedEvent) {
		
	    // If state was saved when executing it now has to
	    // be ready, because oddjob must have crashed last time.
	    if (savedEvent.getState().isStoppable()) {
	    	lastEvent = new StateEvent(source, readyState);
		}
	    else {
	    	lastEvent = new StateEvent(source, savedEvent.getState(), 
	    			savedEvent.getTime(), savedEvent.getException());
	    }
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobState(org.oddjob.state.JobState, java.util.Date)
	 */
	public void setState(S state, Date date) {
		setLastJobStateEvent(new StateEvent(source, state, date, null));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobState(org.oddjob.state.JobState)
	 */
	public void setState(S state) {
		setLastJobStateEvent(new StateEvent(source, state, null));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobStateException(java.lang.Throwable, java.util.Date)
	 */
	public void setStateException(S state, Throwable t, Date date) {
		setLastJobStateEvent(
				new StateEvent(source, state, date, t));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobStateException(java.lang.Throwable)
	 */
	public void setStateException(State state, Throwable ex) {
		setLastJobStateEvent(new StateEvent(source, state, ex));
	}	

	private void setLastJobStateEvent(StateEvent event) {
		assertAlive();
		assertLockHeld();
		
		if (fireing) {
			throw new IllegalStateException(
					"Can't change state from a listener!");
		}
		
		lastEvent = event;		
	}
	
	/**
	 * Return the current state of the job.
	 */
	public State getState() {
		final AtomicReference<State> result = new AtomicReference<State>();
		waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				result.set(lastEvent.getState());
			}
		});
		return result.get();	
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

	public boolean tryToWhen(StateCondition when, Runnable runnable) 
	throws OddjobLockedException {
		if (!lock.tryLock()) {
			throw new OddjobLockedException(lock.toString());
		}
		try {
			return doWhen(when, runnable);
		}
		finally {
			lock.unlock();
		}
	}

	public boolean waitToWhen(StateCondition when, Runnable runnable) {
		lock.lock();
		try {
			return doWhen(when, runnable);
		}
		finally {
			lock.unlock();
		}
		
	}

	/**
	 * Do the work that will be executed when this thread holds
	 * the lock.
	 * 
	 * @param when
	 * @param runnable
	 * 
	 * @return
	 */
	private boolean doWhen(StateCondition when, Runnable runnable) {
		if (when.test(lastEvent.getState())) {			
			runnable.run();
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Sleep.
	 * 
	 * @param time
	 * @throws InterruptedException
	 */
	public void sleep(long time) throws InterruptedException {
		assertLockHeld();
		
		if (time == 0) {
			alarm.await();
		}
		else {
			alarm.await(time, TimeUnit.MILLISECONDS);
		}
	}
	
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
	 */			
	public void addStateListener(final StateListener listener) {
		assertAlive();
		
		waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				// setting pending event stops the listener chaning state.
				listeners.add(listener);
				fireing = true;
				try {
					listener.jobStateChange(lastEvent);
				}
				finally {
					fireing = false;
				}
			}
		});
	}


	/**
	 * Remove a job state listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStateListener(final StateListener listener) {
		waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				listeners.remove(listener);
			}
		});
	}

	/**
	 * The number of listeners.
	 * @return
	 */
	public int listenerCount() {
		final AtomicInteger size = new AtomicInteger();
		waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				size.set(listeners.size());
			}
		});
		return size.get();
	}
	
	/**
	 * Override toString.
	 */
	public String toString() {
		return "JobStateHandler( " + getState().toString() + " )"; 
	}

	/**
	 * Fire the event, update last event.
	 * 
	 * @param event The event.
	 */
	public void fireEvent() {
		assertLockHeld();

		if (fireing) {
			throw new IllegalStateException(
					"Can't fire event from a listener!");
		}
		
		fireing = true;
		try {
			doFireEvent(lastEvent);
		}
		finally {
			fireing = false;
		}
	}
	
	private void doFireEvent(StateEvent event) {
		if (event == null) {
			throw new NullPointerException("No JobStateEvent.");
		}
		
		List<StateListener> copy = new ArrayList<StateListener>(listeners);
		
		for (StateListener listener : copy) {
			try {
				listener.jobStateChange(event);
			}
			catch (Throwable t) {
				logger.error("Failed notifiying listener [" + listener
						+ "] of event [" + event + "]", t);
			}
		}
	}		
	
	/**
	 * Implement custom serialization.
	 * 
	 * @param s The stream.
	 * @throws IOException If serialisation fails.
	 */
	private void writeObject(ObjectOutputStream s)
	throws IOException {
		s.defaultWriteObject();
	}
	
	/**
	 * Implement custom serialization.
	 * 
	 * @param s The stream
	 * @throws IOException If serialisation fails.
	 * @throws ClassNotFoundException If class can't be found.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		listeners = new ArrayList<StateListener>();
	}
	
}

