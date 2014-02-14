package org.oddjob.state;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

/**
 * These are drop in replacements for jobs that used to use 
 * a {@code JobState}s for conditions.
 * 
 * @author Rob Gordon
 */
public enum StateConditions implements StateCondition {
	
	/**
	 * Is a job ready?
	 */	
	READY() {
		@Override
		public boolean test(State state) {
			return state.isReady();
		}
	},

	/**
	 * A state is either {@link JobState#EXECUTING} or 
	 * {@link ParentState#EXECUTING} or {@link ServiceState#STARTING}.
	 */
	EXECUTING() {
		@Override
		public boolean test(State state) {
			return state.isExecuting();
		}
	},
	
	/**
	 * Is a job incomplete?
	 */
	INCOMPLETE() {
		@Override
		public boolean test(State state) {
			return state.isIncomplete();
		}
	},
		
	/**
	 * Is a job complete?
	 */	
	COMPLETE() {
		@Override
		public boolean test(State state) {
			return state.isComplete();
		}
	},
	
	/**
	 * Is a job in an exception state?
	 */	
	EXCEPTION() {
		@Override
		public boolean test(State state) {
			return state.isException();
		}
	},
	
	/**
	 * The job has been destroyed. It can no longer be used. Note that
	 * many Jobs that use state conditions will enter an EXCEPTION state
	 * if the job they trigger on is destroyed, and so this state
	 * condition will not be met.
	 */	
	DESTROYED() {
		@Override
		public boolean test(State state) {
			return state.isDestroyed();
		}
	},
	
	// Composite and Special Conditions
	
	/**
	 * Is a job executing or otherwise active?
	 */	
	RUNNING() {
		@Override
		public boolean test(State state) {
			return state.isStoppable();
		}
	},

	/**
	 * The state is either INCOMPLETE or EXCEPTION
	 */
	FAILURE() {
		@Override
		public boolean test(State state) {
			return state.isIncomplete() || state.isException();
		}
	},
		
	/**
	 * Like {@link #COMPLETE} but also stopped. Applicable to services
	 * which are complete when started.
	 */
	DONE() {
		@Override
		public boolean test(State state) {
			return state.isComplete() && !state.isStoppable();
		}
	},
	
	/**
	 * The state is either COMPLETE, INCOMPLETE or EXCEPTION
	 */	
	FINISHED() {
		@Override
		public boolean test(State state) {
			return state.isComplete() || 
					state.isIncomplete() || 
					state.isException();
		}
	},
	
	/**
	 * Like {@link #FINISHED} but also stopped. Applicable to services
	 * which are complete when started.
	 */
	ENDED() {
		@Override
		public boolean test(State state) {
			return !RUNNING.test(state) && FINISHED.test(state);
		}
	},
	
	/**
	 * A job that is active. Indicates necessary work is still being done 
	 * asynchronously, as opposed to {@link STARTED) which indicates 
	 * necessary work is complete.
	 */
	ACTIVE() {
		@Override
		public boolean test(State state) {
			return LIVE.test(state) && !COMPLETE.test(state);
		}
	},
	
	/**
	 * A job that is stoppable but not executing. Includes active and
	 * started jobs.
	 */
	LIVE() {
		@Override
		public boolean test(State state) {
			return state.isStoppable() && !state.isExecuting();
		}
	},
	
	/**
	 * Something, generally a service, has STARTED when it is both
	 * complete and stoppable.
	 */
	STARTED() {
		@Override
		public boolean test(State state) {
			return state.isComplete() && state.isStoppable();
		}
	},

	;
	
	
	/**
	 * 
	 * The Conversion from String
	 */
	public static class Conversions implements ConversionProvider {
		@Override
		public void registerWith(ConversionRegistry registry) {
			registry.register(String.class, StateCondition.class, 
					new Convertlet<String, StateCondition>() {
				@Override
				public StateCondition convert(String from)
						throws ConvertletException {
					if (from.startsWith("!")) {
						return new IsNot(
								StateConditions.valueOf(
										from.substring(1).toUpperCase()));
					}
					else {
						return StateConditions.valueOf(from.toUpperCase());						
					}
				}
			});
		}
	}	
}

