package org.oddjob.state;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.oddjob.Stateful;
import org.oddjob.util.IO;

/**
 * An instance of this class is produced when a job state changes. It is
 * passed to all JobStateListeners.
 * <p>
 * 
 * @author Rob Gordon
 */
public final class StateEvent {

	public static final String REPLACEMENT_EXCEPTION_TEXT = "Exception is not serializable, message is: ";
	
	/** The source. Note that the source in {@link java.util.EventObject} is
	 * not final and so safe publication to other threads is not
	 * guaranteed. */
	private final Stateful source;
	
	/** The state */
    private final State state;
    
    /** The time the state changed. */
	private final Date time;
	
	/** Any exception that caused an exception state. */
	private final Throwable exception;
	
	/**
	 * Constructor.
	 * 
	 * @param source The source of the event.
	 * @param state The state.
	 * @param time the Time of the event.
	 * @param exception The exception if applicable, or null otherwise.
	 */	
	public StateEvent(Stateful source, State state, Date time, Throwable exception) {
		Objects.requireNonNull(source, "No source");
		Objects.requireNonNull(state ,"JobState can not be null!");
		Objects.requireNonNull(time, "No time");

		if (state.isException()) {
			Objects.requireNonNull(exception, "Exception required if state is exception.");
		}

	    this.source = source;
		this.state = state;
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

	public Stateful getSource() {
		return source;
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
	 * Provide something that can be serialised. Note the we do not use
	 * {@code writeReplace} because there would need to be the corresponding
	 * {@code readResolve}
	 * 
	 * @return A serializable version of the event without the source.
	 */
	public SerializableNoSource serializable() {
		return new SerializableNoSource(getState(), 
				getTime(), getException());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StateEvent that = (StateEvent) o;
		return source.equals(that.source) &&
				state.equals(that.state) &&
				time.equals(that.time) &&
				Objects.equals(exception, that.exception);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, state, time, exception);
	}

	/**
	 * Override toString.
	 */
	public String toString() {
		return "JobStateEvent, source=" + getSource() + ", " + state;
	}
	
	/**
	 * Used to replace a non serializable exception.
	 *
	 */
	static class ExceptionReplacement extends Exception {
		private static final long serialVersionUID = 20051217;
		
		ExceptionReplacement(Throwable replacing) {
			super(REPLACEMENT_EXCEPTION_TEXT + replacing.getMessage());
			super.setStackTrace(replacing.getStackTrace());
		}
	}
	
	/**
	 * Used to persist the event. There is no {@code ReadResolve} method
	 * because it is not possible to resolve without the Stateful source.
	 * <p>
	 * 
	 */
	public static class SerializableNoSource implements Serializable {
		private static final long serialVersionUID = 2014050700L;
		
		private final State state;
		private final Date time;
		private final Throwable exception;
		
		public SerializableNoSource(State state, Date date, 
				Throwable exception) {
			this.state = state;
			this.time = date;
			if (IO.canSerialize(exception)) {
				this.exception = exception;
			}
			else {
				this.exception = new ExceptionReplacement(exception);
			}
		}
		
		public State getState() {
			return state;
		}
		
		public Date getTime() {
			return time;
		}
		
		public Throwable getException() {
			return exception;
		}
	}
}
