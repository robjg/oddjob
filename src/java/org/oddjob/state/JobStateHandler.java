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

public class JobStateHandler 
implements Stateful, StateLock {
	
	private static final Logger logger = Logger.getLogger(JobStateHandler.class);
	
	/** The source. */
	private final Stateful source;
	
	/** State listeners */
	private transient ArrayList<JobStateListener> listeners = 
		new ArrayList<JobStateListener>();

	/** The last event */
	private volatile JobStateEvent lastEvent;

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
	public JobStateHandler(Stateful source) {
		this.source = source;
		lastEvent = new JobStateEvent(source, JobState.READY, null);
	}	
	
	/**
	 * Get the last event.
	 * 
	 * @return The last event.
	 */
	@Override
	public JobStateEvent lastJobStateEvent() {
		final AtomicReference<JobStateEvent> result = 
			new AtomicReference<JobStateEvent>();
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
	public void restoreLastJobStateEvent(JobStateEvent savedEvent) {
		
	    // If state was saved when executing it now has to
	    // be ready, because oddjob must have crashed last time.
	    if (savedEvent.getJobState() == JobState.EXECUTING) {
	    	lastEvent = new JobStateEvent(source, JobState.READY);
		}
	    else {
	    	lastEvent = new JobStateEvent(source, savedEvent.getJobState(), 
	    			savedEvent.getTime(), savedEvent.getException());
	    }
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobState(org.oddjob.state.JobState, java.util.Date)
	 */
	public void setJobState(JobState state, Date date) {
		setLastJobStateEvent(new JobStateEvent(source, state, date, null));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobState(org.oddjob.state.JobState)
	 */
	public void setJobState(JobState state) {
		setLastJobStateEvent(new JobStateEvent(source, state, null));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobStateException(java.lang.Throwable, java.util.Date)
	 */
	public void setJobStateException(Throwable t, Date date) {
		setLastJobStateEvent(
				new JobStateEvent(source, JobState.EXCEPTION, date, t));
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.state.StateChanger#setJobStateException(java.lang.Throwable)
	 */
	public void setJobStateException(Throwable ex) {
		setLastJobStateEvent(new JobStateEvent(source, JobState.EXCEPTION, ex));
	}	

	private void setLastJobStateEvent(JobStateEvent event) {
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
	public JobState getJobState() {
		final AtomicReference<JobState> result = new AtomicReference<JobState>();
		waitToWhen(new IsAnyState(), new Runnable() {
			@Override
			public void run() {
				result.set(lastEvent.getJobState());
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
		if (lastEvent.getJobState() == JobState.DESTROYED) {
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
		if (when.test(lastEvent.getJobState())) {			
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
	public void addJobStateListener(final JobStateListener listener) {
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
	public void removeJobStateListener(final JobStateListener listener) {
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
		return "JobStateHandler( " + getJobState().toString() + " )"; 
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
	
	private void doFireEvent(JobStateEvent event) {
		if (event == null) {
			throw new NullPointerException("No JobStateEvent.");
		}
		
		List<JobStateListener> copy = new ArrayList<JobStateListener>(listeners);
		
		for (JobStateListener listener : copy) {
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
		listeners = new ArrayList<JobStateListener>();
	}
	
}

