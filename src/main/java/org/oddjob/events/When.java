/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import org.oddjob.FailedToStopException;
import org.oddjob.Resetable;
import org.oddjob.Stoppable;

/**
 * @oddjob.description Runs a job when triggered by the arrival of an event.
 * 
 * @oddjob.example
 * 
 * Evaluating greengrocer portfolios of fruit when data arrives.
 * 
 * {@oddjob.xml.resource org/oddjob/events/example/PricingWhenExample.xml}
 * 
 * 
 * @author Rob Gordon.
 */
public class When<T> extends EventJobBase<T> {
	
	private static final long serialVersionUID = 2018060600L; 

	private final AtomicReference<T> eventRef = new AtomicReference<>();

	@Override
	void onImmediateEvent(T event) {
		eventRef.set(event);
	}
	
	@Override
	synchronized void onSubscriptonStarted(Object job, Executor executor) {
		Optional.ofNullable(eventRef.getAndSet(null)).ifPresent(
				e -> trigger(e, job, executor));
	}
	
	@Override
	synchronized void onLaterEvent(T event, Object job, Executor executor) {
		trigger(event, job, executor);
	}
	
	void trigger(T event, Object job, Executor executor) {

		if (job != null) {
			if (job instanceof Stoppable) {

				try {
					((Stoppable) job).stop();
				} catch (FailedToStopException e) {
					throw new RuntimeException("[" + this.toString() + "] failed to stop child [" +
							job + "] for event " + event, e);
				}
			}
		
			if (job instanceof Resetable) {
				((Resetable) job).hardReset();
			}
		}

		setCurrent(event);
		
		if (job != null) {
			if (job != null && job instanceof Runnable) {
				executor.execute((Runnable) job);
			}
		}
	}
}
