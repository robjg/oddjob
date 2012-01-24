package org.oddjob.state;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.EventObject;

import org.oddjob.Stateful;
import org.oddjob.util.IO;

/**
 * An instance of this class is produced when a job state changes. It is
 * passed to all JobStateListeners.
 * 
 * @author Rob Gordon
 */

public class StateEvent extends EventObject 
implements Serializable {

    private static final long serialVersionUID = 20051026;

	static final String REPLACEMENT_EXCEPTION_TEXT = "Exception is not serializable, message is: ";
	
    private State state;
	private Date time;
	private Throwable exception;
	
	/**
	 * Used to replace a non serializable exception.
	 *
	 */
	class ExceptionReplacement extends Exception {
		private static final long serialVersionUID = 20051217;
		public ExceptionReplacement(Throwable replacing) {
			super(REPLACEMENT_EXCEPTION_TEXT + replacing.getMessage());
			super.setStackTrace(exception.getStackTrace());
		}
	}
	
	/**
	 * Constructor.
	 * 
	 * @param job The source of the event.
	 * @param jobState The state.
	 * @param time the Time of the event.
	 * @param exception The exception if applicable, or null otherwise.
	 */	
	public StateEvent(Stateful job, State jobState, Date time, Throwable exception) {
	    super(job);
	    if (jobState == null) {
	    	throw new NullPointerException("JobState can not be null!");
	    }
		this.state = jobState;
		this.time = time;
		this.exception = exception;
	}

	/**
	 * Constructor.
	 * 
	 * @param job The source of the event.
	 * @param jobState The state.
	 * @param exception The exception if applicable, or null otherwise.
	 */
	public StateEvent(Stateful job, State jobState, Throwable exception) {
		this(job, jobState, new Date(), exception);
	}

	/**
	 * Constructor.
	 * 
	 * @param job The source of the event.
	 * @param jobState The state.
	 */
	public StateEvent(Stateful job, State jobState) {
		this(job, jobState, null);
	}

	@Override
	public Stateful getSource() {
		return (Stateful) super.getSource();
	}
	
	/**
	 * Get the job state.
	 * 
	 * @return The job state.
	 */	
	public State getState() {
		return state;	
	}

	/**
	 * Get the exception if applicable, null otherwise.
	 * 
	 * @return The exception of null.
	 */	
	public Throwable getException() {
		return exception;
	}	

	/**
	 * Get the time of the event..
	 * 
	 * @return The time.
	 */
	public Date getTime() {
		return time;
	}

	/**
	 * Override toString.
	 */
	public String toString() {
		return "JobStateEvent, source=" + getSource() + ", " + state;
	}
	
	/*
	 * Custom serialization.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.writeObject(state);
		s.writeObject(time);
		if (IO.canSerialize(exception)) {
			s.writeObject(exception);
		}
		else {
			s.writeObject(new ExceptionReplacement(exception));
		}
	}
	
	/*
	 * Custom serialization.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		state = (State) s.readObject();
		time = (Date) s.readObject();
		exception = (Throwable) s.readObject();
	}
	
}
