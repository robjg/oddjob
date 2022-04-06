/*
 * (c) Rob Gordon 2018
 */
package org.oddjob.events;

import org.oddjob.FailedToStopException;
import org.oddjob.Resettable;
import org.oddjob.Stoppable;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @oddjob.description Runs a job when triggered by the arrival of an event. The job will be re-run every time
 * the event arrives. If the job is still running when a new event arrives, the job will attempt to be stopped
 * and rerun. A typical use case would be processing a file when it arrives, but which may be re-sent with more
 * up-to-date information.
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
	synchronized void onSubscriptionStarted(Object job, Executor executor) {
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
					throw new RuntimeException("[" + this + "] failed to stop child [" +
							job + "] for event " + event, e);
				}
			}
		
			if (job instanceof Resettable) {
				((Resettable) job).hardReset();
			}
		}

		setTrigger(event);
		
		if (job instanceof Runnable) {
				executor.execute((Runnable) job);
		}
	}
}
