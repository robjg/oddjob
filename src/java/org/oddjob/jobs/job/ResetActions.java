package org.oddjob.jobs.job;

import org.oddjob.Forceable;
import org.oddjob.Resetable;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
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
			if (job instanceof Resetable) {
			    ((Resetable) job).hardReset();
			}
		}
	},
	
	/**
	 */
	SOFT() {
		@Override
		public void doWith(Object job) {
			if (job instanceof Resetable) {
			    ((Resetable) job).softReset();
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
					new Convertlet<String, ResetAction>() {
				@Override
				public ResetAction convert(String from) {
					return ResetActions.valueOf(from.toUpperCase());						
				}
			});
		}
	}	
}

