package org.oddjob.jobs.job;

import org.oddjob.Forceable;
import org.oddjob.Resettable;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.state.State;

/**
 * Some Standard {@link ResetAction}s.
 * 
 * @author Rob Gordon
 */
public enum ResetActions implements ResetAction {
	
	/**
	 */	
	NONE() {
		@Override
		public void doWith(Object job) {
		}
	},
	
	/**
	 */	
	AUTO() {
		@Override
		public void doWith(Object job) {
			ResetAction action = NONE;
			if (job instanceof Stateful) {
				State state = ((Stateful) job).lastStateEvent().getState();
				if (state.isComplete()) {
					action = HARD;
				}
				else if (state.isException() || state.isIncomplete()) {
					action = SOFT;
				}
			}
			action.doWith(job);
		}
	},

	/**
	 */
	HARD() {
		@Override
		public void doWith(Object job) {
			if (job instanceof Resettable) {
			    ((Resettable) job).hardReset();
			}
		}
	},
	
	/**
	 */
	SOFT() {
		@Override
		public void doWith(Object job) {
			if (job instanceof Resettable) {
			    ((Resettable) job).softReset();
			}
		}
	},
		
	/**
	 */	
	FORCE() {
		@Override
		public void doWith(Object job) {
			if (job instanceof Forceable) {
				((Forceable) job).force();
			}
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
			registry.register(String.class, ResetAction.class,
					from -> ResetActions.valueOf(from.toUpperCase()));
		}
	}	
}

