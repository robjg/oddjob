package org.oddjob.state;

import org.oddjob.Stateful;
import org.oddjob.util.IO;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

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
	private final StateInstant instant;
	
	/** Any exception that caused an exception state. */
	private final Throwable exception;

	/**
	 * Constructor.
	 *
	 * @param source The source of the event.
	 * @param state The state.
	 * @param instant the Time of the event.
	 * @param exception The exception if applicable, or null otherwise.
	 */
	private StateEvent(Stateful source, State state, StateInstant instant, Throwable exception) {
		this.source = Objects.requireNonNull(source, "No source");
		this.state = Objects.requireNonNull(state ,"JobState can not be null!");
		this.instant = Objects.requireNonNull(instant, "No time");

		if (state.isException()) {
			this.exception = Objects.requireNonNull(exception, "Exception required if state is exception.");
		}
		else {
			if (exception != null) {
				throw new IllegalStateException("Exception can only be set when state is Exception");
			}
			this.exception = null;
		}
	}

	/**
	 * Constructor. Deprecated - use {@link #exceptionAtInstant(Stateful, State, StateInstant, Throwable)}
	 * 
	 * @param source The source of the event.
	 * @param state The state.
	 * @param time the Time of the event.
	 * @param exception The exception if applicable, or null otherwise.
	 */
	@Deprecated(since="1.7", forRemoval=true)
	public StateEvent(Stateful source, State state, Date time, Throwable exception) {
		this(source, state, StateInstant.forOneVersionOnly(time.toInstant()), exception);
	}

	/**
	 * Constructor. Deprecated - use {@link #exceptionNow(Stateful, State, Throwable)}
	 * 
	 * @param job The source of the event.
	 * @param jobState The state.
	 * @param exception The exception if applicable, or null otherwise.
	 */
	@Deprecated(since="1.7", forRemoval=true)
	public StateEvent(Stateful job, State jobState, Throwable exception) {
		this(job, jobState, StateInstant.now(), exception);
	}

	/**
	 * Constructor. Deprecated - {@link #now(Stateful, State)}
	 * 
	 * @param job The source of the event.
	 * @param jobState The state.
	 */
	@Deprecated(since="1.7", forRemoval=true)
	public StateEvent(Stateful job, State jobState) {
		this(job, jobState,
				StateInstant.now(), null);
	}

	/**
	 * Create a new State Event at the given instant.
	 *
	 * @param source The source of the event.
	 * @param state The state.
	 * @param instant The date time of the event.
	 *
	 * @return A new State Event.
	 */
	public static StateEvent atInstant(Stateful source,
									   State state,
									   StateInstant instant) {
		return new StateEvent(source, state, instant, null);
	}

	/**
	 * Create a new State Event with an instant of now.
	 *
	 * @param source The source of the event.
	 * @param state The state.
	 *
	 * @return A new State Event.
	 */
	public static StateEvent now(Stateful source,
								 State state) {
		return new StateEvent(source, state, StateInstant.now(), null);
	}

	/**
	 * Create a new State Event at the given instant with the given Exception.
	 * The state is expected to have {@link @State#isException} true, but this
	 * isn't validated.
	 *
	 * @param source The source of the event.
	 * @param exceptionState The state.
	 * @param instant The date time of the event.
	 * @param exception
	 *
	 * @return A new State Event.
	 */
	public static StateEvent exceptionAtInstant(Stateful source,
												State exceptionState,
												StateInstant instant,
												Throwable exception) {
		return new StateEvent(source, exceptionState, instant, exception);
	}

	/**
	 * Create a new State Event with an instant of now and the given Exception.
	 * The state is expected to have {@link @State#isException} true, but this
	 * isn't validated.
	 *
	 * @param source The source of the event.
	 * @param exceptionState The state.
	 * @param exception
	 *
	 * @return A new State Event.
	 */
	public static StateEvent exceptionNow(Stateful source,
										  State exceptionState,
										  Throwable exception) {
		return new StateEvent(source, exceptionState, StateInstant.now(), exception);
	}

	/**
	 * Create a new State Event with the given source and the other details
	 * taken from the State Detail.
	 *
	 * @param source The source of the event.
	 * @param stateDetail Provide the other details of the event.
	 *
	 * @return A new State Event.
	 */
	public static StateEvent fromDetail(Stateful source, StateDetail stateDetail) {
		return new StateEvent(source, stateDetail.getState(),
				stateDetail.getStateInstant(), stateDetail.getException());
	}

	/**
	 * Create a mutable copy of the event.
	 *
	 * @return A mutable copy.
	 */
	public Clone copy() {
		return new Clone(this);
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
	 * Get the time of the event.
	 * Deprecated - used {@link #getInstant()}.
	 *
	 * @return The time.
	 */
	@Deprecated(since="1.7", forRemoval=true)
	public Date getTime() {
		return Date.from(instant.getInstant());
	}

	/**
	 * Get the time of the event as an instant.
	 *
	 * @return The time.
	 */
	public Instant getInstant() {
		return instant.getInstant();
	}

	/**
	 * Get the time of the event as a State Instant.
	 *
	 * @return The State Instant.
	 */
	public StateInstant getStateInstant() {
		return instant;
	}

	/**
	 * Provide something that can be serialised. Note that we do not use
	 * {@code writeReplace} because there would need to be the corresponding
	 * {@code readResolve}
	 * 
	 * @return A serializable version of the event without the source.
	 */
	public StateDetail serializable() {
		return new SerializableNoSource(state, instant, exception);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StateEvent that = (StateEvent) o;
		return source.equals(that.source) &&
				state.equals(that.state) &&
				instant.equals(that.instant) &&
				Objects.equals(exception, that.exception);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source, state, instant, exception);
	}

	/**
	 * Override toString.
	 */
	public String toString() {
		return "JobStateEvent, source=" + getSource() + ", " + state + " at " + instant.getInstant();
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
	static class SerializableNoSource implements Serializable, StateDetail {
		private static final long serialVersionUID = 2023061400L;
		
		private final State state;
		private final StateInstant time;
		private final Throwable exception;

		SerializableNoSource(State state, StateInstant date,
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

		@Override
		public State getState() {
			return state;
		}

		@Deprecated(since="1.7", forRemoval=true)
		public Date getTime() {
			return Date.from(time.getInstant());
		}

		public Instant getInstant() {
			return time.getInstant();
		}

		@Override
		public StateInstant getStateInstant() {
			return time;
		}

		@Override
		public Throwable getException() {
			return exception;
		}

		@Override
		public StateEvent toEvent(Stateful source) {

			return new StateEvent(source, state, time, exception);
		}
	}

	public static class Clone {

		private Stateful source;

		/** The state */
		private State state;

		/** The time the state changed. */
		private StateInstant instant;

		/** Any exception that caused an exception state. */
		private Throwable exception;

		private Clone(StateEvent stateEvent) {
			this.source = stateEvent.source;
			this.state = stateEvent.state;
			this.instant = stateEvent.instant;
			this.exception = stateEvent.exception;
		}

		public Clone withSource(Stateful source) {
			this.source = source;
			return this;
		}

		public Clone withState(State state) {
			this.state = state;
			return this;
		}

		public Clone withException(Throwable exception) {
			this.exception = exception;
			return this;
		}

		public Clone withExceptionState(State state, Throwable exception) {
			this.state = state;
			this.exception = exception;
			return this;
		}

		public Clone withInstant(StateInstant stateInstant) {
			this.instant = stateInstant;
			return this;
		}

		public StateEvent create() {
			return new StateEvent(source, state, instant, exception);
		}
	}
}
