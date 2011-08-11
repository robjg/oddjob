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
	 * Is a job executing or otherwise active?
	 */	
	EXECUTING() {
		@Override
		public boolean test(State state) {
			return state.isStoppable();
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
	 * The job has been destroyed. It can no longer be used.
	 */	
	DESTROYED() {
		@Override
		public boolean test(State state) {
			return state.isDestroyed();
		}
	},
	;
	
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

